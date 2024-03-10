package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import io.quarkus.arc.*;
import jakarta.persistence.*;
import lombok.experimental.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@UtilityClass
public class LectureUpdateProcessor
{
    public static boolean when(Action action, ActionHelper helper)
    {
        return action.<LectureDataDiff>getData().orElseThrow().value().hasDiff();
    }

    public static ActionResult<LectureDataDiff> apply(Action action, ActionHelper helper)
    {
        EntityManager entityManager = Arc.container().select(EntityManager.class).get();
        RoomService roomService = Arc.container().select(RoomService.class).get();

        LectureDataDiff diff = action.<LectureDataDiff>getData().orElseThrow().value();
        LectureData next = diff.getNext();

        LectureEntity lecture = entityManager.createQuery(
                                                     "SELECT lecture FROM Lecture lecture WHERE lecture.lectureId = " + ":lectureId", LectureEntity.class)
                                             .setParameter("lectureId", next.getId())
                                             .getSingleResult();

        lecture.setName(next.getName());
        lecture.setCourse(next.getCourse().orElse(null));
        lecture.setType(next.getType());
        lecture.setStartsAt(next.getStartsAt().atZoneSameInstant(ZoneId.systemDefault()));
        lecture.setEndsAt(next.getEndsAt().atZoneSameInstant(ZoneId.systemDefault()));
        lecture.setRooms(next.getRooms()
                             .stream()
                             .map(roomService::findIdByName)
                             .map(Optional::orElseThrow)
                             .map(item -> RoomEntity.builder().id(item.getValue()).build())
                             .collect(Collectors.toSet()));

        return helper.createResult(diff);
    }
}
