package de.cybine.stuvapi.relay.service.stuv;

import com.fasterxml.jackson.core.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.action.data.*;
import de.cybine.stuvapi.relay.service.lecture.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.cybine.stuvapi.relay.service.action.BaseActionProcessStatus.*;

@Log4j2
@ApplicationScoped
@AllArgsConstructor
public class StuvApiService
{
    public static final ActionMetadata SYNC_METADATA = ActionMetadata.builder()
                                                                     .namespace("stuvapi-relay")
                                                                     .category("lecture")
                                                                     .name("sync")
                                                                     .build();

    private final StuvApi           stuvApi;
    private final ActionService     actionService;
    private final ConverterRegistry converterRegistry;

    private final EntityManager           entityManager;
    private final ActionProcessorRegistry actionProcessorRegistry;

    private final RoomService roomService;

    private final GenericDatasourceService<LectureEntity, Lecture> lectureService = GenericDatasourceService.forType(
            LectureEntity.class, Lecture.class);

    @PostConstruct
    void setup( )
    {
        this.actionProcessorRegistry.registerProcessor(
                new RoomRegistrationProcessor(this.entityManager, this.converterRegistry, this.roomService));
        this.actionProcessorRegistry.registerProcessor(
                new LectureUpdateProcessor(this.entityManager, this.roomService));
        this.actionProcessorRegistry.registerProcessor(new LectureRemovalProcessor(this.entityManager));
        this.actionProcessorRegistry.registerProcessor(
                new LectureRegistrationProcessor(this.entityManager, this.converterRegistry));

        ActionProcessorMetadata termination = ActionProcessorMetadata.builder()
                                                                     .namespace(SYNC_METADATA.getNamespace())
                                                                     .category(SYNC_METADATA.getCategory())
                                                                     .name(SYNC_METADATA.getName())
                                                                     .toStatus(TERMINATED.getName())
                                                                     .build();

        this.actionProcessorRegistry.registerProcessor(GenericActionProcessor.of(termination));
    }

    /**
     * Performs sync of all lectures by fetching original data from StuvAPI
     *
     * @throws InterruptedException
     *         indicates that request was interrupted
     */
    public void updateAll( ) throws InterruptedException, JsonProcessingException
    {
        ZonedDateTime beginOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

        // @formatter:off
        Map<Long, LectureData> persistentLectures =
                this.fetchLecturesEndingAfter(beginOfDay).stream()
                    .collect(Collectors.toMap(LectureData::getId, Function.identity()));

        Map<Long, LectureData> updateLectures =
                this.stuvApi.fetchLectures(true,
                                item -> item.getEndsAt().isAfter(beginOfDay.toOffsetDateTime()))
                            .stream()
                            .collect(Collectors.toMap(LectureData::getId, Function.identity()));
        // @formatter:on

        log.info("Starting lecture sync...");
        log.info("Processing {} persisted and {} fetched lectures", persistentLectures.size(), updateLectures.size());

        ActionContext context = this.actionService.createContext(ActionContextMetadata.of(SYNC_METADATA));

        log.info("Registering unknown rooms...");
        this.registerRooms(context.getId(), updateLectures.values()
                                                          .stream()
                                                          .map(LectureData::getRooms)
                                                          .flatMap(Collection::stream)
                                                          .distinct()
                                                          .toList());

        log.info("Registering unknown lectures...");
        this.actionService.bulkProcess(updateLectures.values()
                                                     .stream()
                                                     .filter(item -> !persistentLectures.containsKey(item.getId()))
                                                     .map(item -> ActionProcessMetadata.builder()
                                                                                       .contextId(context.getId())
                                                                                       .status(LectureRegistrationProcessor.ACTION)
                                                                                       .createdAt(ZonedDateTime.now())
                                                                                       .data(ActionData.of(item))
                                                                                       .build())
                                                     .toList(), true);

        log.info("Updating known lectures...");
        this.actionService.bulkProcess(updateLectures.values()
                                                     .stream()
                                                     .filter(item -> persistentLectures.containsKey(item.getId()))
                                                     .map(item -> LectureDataDiff.of(
                                                             persistentLectures.get(item.getId()), item))
                                                     .map(item -> ActionProcessMetadata.builder()
                                                                                       .contextId(context.getId())
                                                                                       .status(LectureUpdateProcessor.ACTION)
                                                                                       .createdAt(ZonedDateTime.now())
                                                                                       .data(ActionData.of(item))
                                                                                       .build())
                                                     .toList(), true);

        log.info("Removing canceled lectures...");
        List<LectureData> removedLectures = persistentLectures.values()
                                                              .stream()
                                                              .filter(item -> !updateLectures.containsKey(item.getId()))
                                                              .toList();

        this.actionService.bulkProcess(removedLectures.stream()
                                                      .map(item -> ActionProcessMetadata.builder()
                                                                                        .contextId(context.getId())
                                                                                        .status(LectureRemovalProcessor.ACTION)
                                                                                        .createdAt(ZonedDateTime.now())
                                                                                        .data(ActionData.of(item))
                                                                                        .build())
                                                      .toList(), true);

        this.actionService.terminateContext(context.getId());
        log.info("Finished syncing lectures.");
    }

    private List<LectureData> fetchLecturesEndingAfter(ZonedDateTime dateTime)
    {
        DatasourceConditionDetail<ZonedDateTime> isAfterStartOfDay = DatasourceHelper.isGreater(LectureEntity_.ENDS_AT,
                dateTime);

        DatasourceConditionDetail<LectureStatus> isNotArchived = DatasourceHelper.isNotEqual(LectureEntity_.STATUS,
                LectureStatus.ARCHIVED);

        DatasourceQuery query = DatasourceQuery.builder()
                                               .condition(DatasourceHelper.and(isAfterStartOfDay, isNotArchived))
                                               .relation(DatasourceHelper.fetch(LectureEntity_.ROOMS))
                                               .build();

        ConverterConstraint constraint = ConverterConstraint.builder().allowEmptyCollection(true).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();

        List<LectureEntity> entities = this.lectureService.fetchEntities(query);
        List<Lecture> lectures = this.converterRegistry.getProcessor(LectureEntity.class, Lecture.class, tree)
                                                       .toList(entities)
                                                       .result();

        return this.converterRegistry.getProcessor(Lecture.class, LectureData.class, tree).toList(lectures).result();
    }

    private void registerRooms(ActionContextId contextId, List<String> roomNames)
    {
        Set<String> knownNames = this.roomService.getKnownNames();
        this.actionService.bulkProcess(roomNames.stream()
                                                .filter(item -> !knownNames.contains(item))
                                                .map(item -> Room.builder().name(item).id(RoomId.create()).build())
                                                .map(item -> ActionProcessMetadata.builder()
                                                                                  .contextId(contextId)
                                                                                  .status(RoomRegistrationProcessor.ACTION)
                                                                                  .createdAt(ZonedDateTime.now())
                                                                                  .data(ActionData.of(item))
                                                                                  .build())
                                                .toList(), true);
    }
}
