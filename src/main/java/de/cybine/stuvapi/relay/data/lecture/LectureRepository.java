package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.stuvapi.relay.exception.api.*;
import jakarta.enterprise.context.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

@ApplicationScoped
@AllArgsConstructor
public class LectureRepository
{
    private final EntityManager entityManager;

    public List<LectureEntity> getAllLectures( )
    {
        return this.entityManager.createQuery(
                "SELECT lecture FROM Lecture lecture LEFT JOIN FETCH lecture.rooms WHERE lecture.status <> " +
                        "'ARCHIVED'" + " ORDER BY lecture.startsAt",
                LectureEntity.class).getResultList();
    }

    public List<LectureEntity> getAllLectures(String course, ZonedDateTime from, ZonedDateTime until)
    {
        return this.entityManager.createQuery(
                           "SELECT lecture FROM Lecture lecture LEFT JOIN FETCH lecture.rooms WHERE lecture.status " + "<>" + " 'ARCHIVED' AND (:course IS NULL OR lecture.course LIKE :course) AND " + "(:from IS " + "NULL OR lecture.endsAt >= :from) AND (:until IS NULL OR lecture" + ".startsAt <= " + ":until) ORDER BY lecture.startsAt",
                           LectureEntity.class)
                                 .setParameter("course", course)
                                 .setParameter("from", from)
                                 .setParameter("until", until)
                                 .getResultList();
    }

    public Optional<LectureEntity> getLectureById(LectureId id)
    {
        return this.entityManager.createQuery(
                "SELECT lecture FROM Lecture lecture LEFT JOIN FETCH lecture.rooms WHERE lecture.id = :id",
                LectureEntity.class).setParameter("id", id.getValue()).getResultStream().findAny();
    }

    public Set<LectureEntity> fetchByLectureIds(List<Long> lectureIds)
    {
        throw new NotImplementedException();
    }
}
