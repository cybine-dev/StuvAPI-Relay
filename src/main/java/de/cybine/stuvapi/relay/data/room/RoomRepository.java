package de.cybine.stuvapi.relay.data.room;

import jakarta.enterprise.context.*;
import jakarta.persistence.*;
import lombok.*;

@ApplicationScoped
@RequiredArgsConstructor
public class RoomRepository
{
    private final EntityManager entityManager;

    public void persist(RoomEntity entity)
    {
        this.entityManager.persist(entity);
    }
}
