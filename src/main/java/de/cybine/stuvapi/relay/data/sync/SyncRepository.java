package de.cybine.stuvapi.relay.data.sync;

import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class SyncRepository
{
    private final EntityManager entityManager;

    private final SyncMapper syncMapper;

    public List<SyncDto> getAll( )
    {
        return this.entityManager.createQuery(
                "SELECT sync FROM Sync sync LEFT JOIN FETCH sync.data data LEFT JOIN FETCH data.lecture",
                Sync.class).getResultList().stream().map(this.syncMapper::toData).toList();
    }

    public List<UUID> getAllIds( )
    {
        return this.entityManager.createQuery("SELECT sync.id FROM Sync sync", UUID.class).getResultList();
    }

    public Optional<SyncDto> getById(UUID id)
    {
        return this.entityManager.createQuery(
                "SELECT sync FROM Sync sync LEFT JOIN FETCH sync.data data LEFT JOIN FETCH data.lecture WHERE sync.id = :id",
                Sync.class).setParameter("id", id).getResultStream().findAny().map(this.syncMapper::toData);
    }
}
