package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.stuv.*;
import de.cybine.stuvapi.relay.util.converter.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.log4j.*;

@Log4j2
@RequiredArgsConstructor
public class RoomRegistrationProcessor implements ActionProcessor<Room>
{
    public static final String ACTION = "register-room";

    private final EntityManager     entityManager;
    private final ConverterRegistry converterRegistry;

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
        return true;
    }

    @Override
    public ActionProcessorResult<Room> process(ActionStateTransition transition)
    {
        Room room = transition.getNextState().<Room>getData().orElseThrow().value();

        log.debug("Registering new room '{}'", room.getName());
        this.roomService.registerRoomId(room.getName(), room.getId());
        this.entityManager.persist(
                this.converterRegistry.getProcessor(Room.class, RoomEntity.class).toItem(room).result());

        return ActionProcessorResult.of(room);
    }
}
