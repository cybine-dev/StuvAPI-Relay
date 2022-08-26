package de.cybine.stuvapi.relay.data.room;

import lombok.Builder;
import lombok.Data;

import java.util.Optional;
import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class RoomDto
{
    private final UUID id;

    private final String name;
    private final String displayName;

    public Optional<UUID> getId( )
    {
        return Optional.ofNullable(this.id);
    }

    public String getDisplayName( )
    {
        return this.displayName == null ? this.name : this.displayName;
    }
}
