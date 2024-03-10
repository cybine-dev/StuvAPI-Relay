package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.stuvapi.relay.data.room.*;
import io.quarkus.arc.*;
import jakarta.persistence.*;
import lombok.experimental.*;
import lombok.extern.slf4j.*;

@Slf4j
@UtilityClass
public class RoomRegistrationProcessor
{
    public static ActionResult<Room> apply(Action action, ActionHelper helper)
    {
        EntityManager entityManager = Arc.container().select(EntityManager.class).get();
        ConverterRegistry converterRegistry = Arc.container().select(ConverterRegistry.class).get();
        RoomService roomService = Arc.container().select(RoomService.class).get();

        Room room = action.<Room>getData().orElseThrow().value();

        log.debug("Registering new room '{}'", room.getName());
        roomService.registerRoomId(room.getName(), room.getId());
        entityManager.persist(converterRegistry.getProcessor(Room.class, RoomEntity.class).toItem(room).result());

        return helper.createResult(room);
    }
}
