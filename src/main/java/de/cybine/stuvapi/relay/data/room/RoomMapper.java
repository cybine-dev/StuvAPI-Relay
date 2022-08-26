package de.cybine.stuvapi.relay.data.room;

import de.cybine.stuvapi.relay.data.EntityMapper;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@AllArgsConstructor
public class RoomMapper implements EntityMapper<Room, RoomDto>
{
    @Override
    public Room toEntity(final RoomDto data)
    {
        return Room.builder()
                .id(data.getId().orElse(null))
                .name(data.getName())
                .displayName(data.getDisplayName().equals(data.getName()) ? null : data.getDisplayName())
                .build();
    }

    @Override
    public RoomDto toData(final Room entity)
    {
        return RoomDto.builder().id(entity.getId()).name(entity.getName()).displayName(entity.getDisplayName()).build();
    }
}
