package de.cybine.stuvapi.relay.data.sync;

import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.List;

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
                Sync.class).getResultStream().toList().stream().map(this.syncMapper::toData).toList();
    }
}
