package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.stuv.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@RequiredArgsConstructor
public class LectureUpdateProcessor implements ActionProcessor<LectureDataDiff>
{
    public static final String ACTION = "lecture-update";

    private final EntityManager entityManager;

    private final RoomService roomService;

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
        return transition.getNextState().<LectureDataDiff>getData().orElseThrow().value().hasDiff();
    }

    @Override
    public ActionProcessorResult<LectureDataDiff> process(ActionStateTransition transition)
    {
        LectureDataDiff diff = transition.getNextState().<LectureDataDiff>getData().orElseThrow().value();
        LectureData next = diff.getNext();

        LectureEntity lecture = this.entityManager.createQuery(
                                            "SELECT lecture FROM Lecture lecture WHERE lecture.lectureId = " +
                                                    ":lectureId", LectureEntity.class)
                                                  .setParameter("lectureId", next.getId())
                                                  .getSingleResult();

        lecture.setName(next.getName());
        lecture.setCourse(next.getCourse().orElse(null));
        lecture.setType(next.getType());
        lecture.setStartsAt(next.getStartsAt().atZoneSameInstant(ZoneId.systemDefault()));
        lecture.setEndsAt(next.getEndsAt().atZoneSameInstant(ZoneId.systemDefault()));
        lecture.setRooms(next.getRooms()
                             .stream()
                             .map(this.roomService::findIdByName)
                             .map(Optional::orElseThrow)
                             .map(item -> RoomEntity.builder().id(item.getValue()).build())
                             .collect(Collectors.toSet()));

        return ActionProcessorResult.of(diff);
    }
}
