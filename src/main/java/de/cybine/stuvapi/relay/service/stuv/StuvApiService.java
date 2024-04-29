package de.cybine.stuvapi.relay.service.stuv;

import com.fasterxml.jackson.core.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.action.stateful.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.lecture.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static de.cybine.quarkus.util.action.ActionProcessorBuilder.*;
import static de.cybine.quarkus.util.action.data.ActionProcessorMetadata.*;
import static de.cybine.stuvapi.relay.service.action.ActionService.*;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class StuvApiService
{
    public static final String ACTION_NAMESPACE = "stuvapi-relay";
    public static final String ACTION_CATEGORY  = "lecture";

    public static final String SYNC_ACTION_NAME = "sync";

    public static final String REGISTER_ROOM_ACTION    = "register-room";
    public static final String REGISTER_LECTURE_ACTION = "register-lecture";
    public static final String LECTURE_UPDATE_ACTION   = "lecture-update";
    public static final String LECTURE_REMOVAL_ACTION  = "lecture-removal";

    private final StuvApi           stuvApi;
    private final RoomService       roomService;
    private final ActionService     actionService;
    private final ApplicationConfig config;
    private final ConverterRegistry converterRegistry;

    private final GenericDatasourceService<LectureEntity, Lecture> lectureService = GenericDatasourceService.forType(
            LectureEntity.class, Lecture.class);

    @PostConstruct
    void setup( )
    {
        // @formatter:off
        WorkflowBuilder.create(ACTION_NAMESPACE, ACTION_CATEGORY, SYNC_ACTION_NAME)
                       .type(WorkflowType.ACTION)
                       .with(on(REGISTER_ROOM_ACTION).from(ANY).apply(RoomRegistrationProcessor::apply))
                       .with(on(REGISTER_LECTURE_ACTION).from(ANY).apply(LectureRegistrationProcessor::apply))
                       .with(on(LECTURE_UPDATE_ACTION).from(ANY).apply(LectureUpdateProcessor::apply).when(LectureUpdateProcessor::when))
                       .with(on(LECTURE_REMOVAL_ACTION).from(ANY).apply(LectureRemovalProcessor::apply))
                       .with(on(TERMINATED_STATE).from(ANY))
                       .apply(this.actionService);
        // @formatter:on
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

        String correlationId = this.actionService.beginWorkflow(ACTION_NAMESPACE, ACTION_CATEGORY, SYNC_ACTION_NAME);

        log.info("Registering unknown rooms...");
        this.registerRooms(correlationId, updateLectures.values()
                                                        .stream()
                                                        .map(LectureData::getRooms)
                                                        .flatMap(Collection::stream)
                                                        .distinct()
                                                        .toList());

        log.info("Registering unknown lectures...");
        this.actionService.bulkPerform(updateLectures.values()
                                                     .stream()
                                                     .filter(item -> !persistentLectures.containsKey(item.getId()))
                                                     .map(ActionData::of)
                                                     .map(item -> Action.of(this.createSyncMetadata(correlationId,
                                                             REGISTER_LECTURE_ACTION), item))
                                                     .toList());

        log.info("Updating known lectures...");
        this.actionService.bulkPerform(updateLectures.values()
                                                     .stream()
                                                     .filter(item -> persistentLectures.containsKey(item.getId()))
                                                     .map(item -> LectureDataDiff.of(
                                                             persistentLectures.get(item.getId()), item))
                                                     .map(ActionData::of)
                                                     .map(item -> Action.of(this.createSyncMetadata(correlationId,
                                                             LECTURE_UPDATE_ACTION), item))
                                                     .toList());

        log.info("Removing canceled lectures...");
        this.actionService.bulkPerform(persistentLectures.values()
                                                         .stream()
                                                         .filter(item -> !updateLectures.containsKey(item.getId()))
                                                         .map(ActionData::of)
                                                         .map(item -> Action.of(this.createSyncMetadata(correlationId,
                                                                 LECTURE_REMOVAL_ACTION), item))
                                                         .toList());

        this.actionService.perform(Action.of(this.createSyncMetadata(correlationId, TERMINATED_STATE), null));
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
        return this.converterRegistry.getProcessor(LectureEntity.class)
                                     .withTree(tree)
                                     .withIntermediary(Lecture.class)
                                     .withOutput(LectureData.class)
                                     .toList(entities)
                                     .result();
    }

    private void registerRooms(String correlationId, List<String> roomNames)
    {
        Set<String> knownNames = this.roomService.getKnownNames();
        this.actionService.bulkPerform(roomNames.stream()
                                                .filter(item -> !knownNames.contains(item))
                                                .map(item -> Room.builder().name(item).id(RoomId.create()).build())
                                                .map(ActionData::of)
                                                .map(item -> Action.of(
                                                        this.createSyncMetadata(correlationId, REGISTER_ROOM_ACTION),
                                                        item))
                                                .toList());
    }

    private ActionMetadata createSyncMetadata(String correlationId, String action)
    {
        return ActionMetadata.builder()
                             .namespace(ACTION_NAMESPACE)
                             .category(ACTION_CATEGORY)
                             .name(SYNC_ACTION_NAME)
                             .correlationId(correlationId)
                             .source(this.config.serviceName())
                             .action(action)
                             .build();
    }
}
