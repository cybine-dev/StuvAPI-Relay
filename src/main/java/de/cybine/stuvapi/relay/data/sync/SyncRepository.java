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

    public List<SyncDto.LectureSync> getDetailsById(UUID id, int limit, int offset)
    {
        return this.entityManager.createQuery(
                        "SELECT DISTINCT data FROM Sync sync JOIN sync.data data LEFT JOIN FETCH data.lecture lecture LEFT JOIN FETCH lecture.rooms WHERE sync.id = :id ORDER BY data.id",
                        Sync.LectureSync.class)
                .setParameter("id", id)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(this.lectureSyncMapper::toData)
                .toList();
    }

    public long getDetailCount(UUID id)
    {
        return this.entityManager.createQuery(
                "SELECT COUNT(data) FROM Sync sync JOIN sync.data data WHERE sync.id = :id",
                Long.class).setParameter("id", id).getSingleResult();
    }
}
