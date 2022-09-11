package de.cybine.stuvapi.relay.data.lecture;

import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class LectureRepository
{
    private final EntityManager entityManager;

    private final LectureMapper lectureMapper;

    public List<LectureDto> getAllLectures( )
    {
        return this.entityManager.createQuery(
                "SELECT lecture FROM Lecture lecture LEFT JOIN FETCH lecture.rooms WHERE lecture.isArchived IS FALSE",
                Lecture.class).getResultList().stream().map(this.lectureMapper::toData).toList();
    }

    public List<LectureDto> getAllLectures(String course, ZonedDateTime from, ZonedDateTime until)
    {
        return this.entityManager.createQuery(
                        "SELECT lecture FROM Lecture lecture LEFT JOIN FETCH lecture.rooms WHERE lecture.isArchived IS FALSE AND (:course IS NULL OR lecture.course LIKE :course) AND (:from IS NULL OR lecture.endsAt >= :from) AND (:until IS NULL OR lecture.startsAt <= :until)",
                        Lecture.class)
                .setParameter("course", course)
                .setParameter("from", from)
                .setParameter("until", until)
                .getResultList()
                .stream()
                .map(this.lectureMapper::toData)
                .toList();
    }

    public Optional<LectureDto> getLectureById(UUID id)
    {
        return this.entityManager.createQuery(
                "SELECT lecture FROM Lecture lecture LEFT JOIN FETCH lecture.rooms WHERE lecture.id = :id",
                Lecture.class).setParameter("id", id).getResultStream().findAny().map(this.lectureMapper::toData);
    }
}
