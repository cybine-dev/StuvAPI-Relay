package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.stuv.*;
import jakarta.persistence.*;
import lombok.*;

@RequiredArgsConstructor
public class LectureRemovalProcessor implements ActionProcessor<LectureData>
{
    public static final String ACTION = "lecture-removal";

    private final EntityManager entityManager;

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
        this.entityManager.createNativeQuery(
                    String.format("UPDATE %s SET %s = :status WHERE %s = :id", LectureEntity_.TABLE,
                            LectureEntity_.STATUS_COLUMN, LectureEntity_.LECTURE_ID_COLUMN), Void.class)
                          .setParameter("status", LectureStatus.ARCHIVED.name())
                          .setParameter("id", data.getId())
                          .executeUpdate();

        return ActionProcessorResult.of(data);
    }
}
