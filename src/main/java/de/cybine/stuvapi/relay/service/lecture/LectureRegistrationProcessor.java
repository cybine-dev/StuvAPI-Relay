package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.stuv.*;
import de.cybine.stuvapi.relay.util.converter.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.*;

@Log4j2
@RequiredArgsConstructor
public class LectureRegistrationProcessor implements ActionProcessor<LectureData>
{
    public static final String ACTION = "register-lecture";

    private final EntityManager     entityManager;
    private final ConverterRegistry converterRegistry;

    @Override
    public ActionProcessorMetadata getMetadata( )
    {
        return ActionProcessorMetadata.builder()
                                      .namespace(StuvApiService.SYNC_METADATA.getNamespace())
                                      .category(StuvApiService.SYNC_METADATA.getCategory())
                                      .name(StuvApiService.SYNC_METADATA.getName())
                                      .toStatus(ACTION)
                                      .build();
    }

    @Override
    public boolean shouldExecute(ActionStateTransition transition)
    {
        return true;
    }

    @Override
    public ActionProcessorResult<LectureData> process(ActionStateTransition transition)
    {
        LectureData data = transition.getNextState().<LectureData>getData().orElseThrow().value();

        Lecture lecture = this.converterRegistry.getProcessor(LectureData.class, Lecture.class)
                                                .withContext("lecture-registered", false)
                                                .toItem(data)
                                                .result();

        this.entityManager.persist(
                this.converterRegistry.getProcessor(Lecture.class, LectureEntity.class).toItem(lecture).result());

        return ActionProcessorResult.of(data);
    }
}
