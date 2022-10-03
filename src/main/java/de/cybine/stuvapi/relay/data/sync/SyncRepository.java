package de.cybine.stuvapi.relay.data.sync;

import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class SyncRepository
{
    private final EntityManager entityManager;

    private final SyncMapper        syncMapper;
    private final LectureSyncMapper lectureSyncMapper;

    public List<SyncDto> getSyncs(int limit, int offset)
    {
        return this.entityManager.createQuery("SELECT sync FROM Sync sync ORDER BY sync.startedAt", Sync.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(this.syncMapper::toData)
                .toList();
    }

    public long getSyncCount( )
    {
        return this.entityManager.createQuery("SELECT COUNT(sync) FROM Sync sync", Long.class).getSingleResult();
    }

    public List<SyncDto.LectureSync> getDetailsById(UUID id, String course, int limit, int offset)
    {
        List<UUID> ids = this.entityManager.createQuery(
                        "SELECT data.id FROM Sync sync JOIN sync.data data WHERE sync.id = :id AND (:course IS NULL OR data.lecture.course = :course) ORDER BY data.id",
                        UUID.class)
                .setParameter("id", id)
                .setParameter("course", course)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        return this.entityManager.createQuery(
                        "SELECT DISTINCT data FROM Sync sync JOIN sync.data data LEFT JOIN FETCH data.lecture lecture LEFT JOIN FETCH lecture.rooms WHERE data.id in (:ids) ORDER BY data.id",
                        Sync.LectureSync.class)
                .setParameter("ids", ids)
                .getResultList()
                .stream()
                .map(this.lectureSyncMapper::toData)
                .toList();
    }

    public long getDetailCount(UUID id, String course)
    {
        return this.entityManager.createQuery(
                "SELECT COUNT(data) FROM Sync sync JOIN sync.data data WHERE sync.id = :id AND (:course IS NULL OR data.lecture.course = :course)",
                Long.class).setParameter("course", course).setParameter("id", id).getSingleResult();
    }
}
