package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.stuvapi.relay.data.room.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder(builderClassName = "Builder")
public class LectureDto
{
    private final UUID id;
    private final long lectureId;

    private final String name;
    private final String course;
    private final String lecturer;

    private final Type type;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime startsAt;
    private final LocalDateTime endsAt;

    private final Set<RoomDto> rooms;

    public Optional<UUID> getId( )
    {
        return Optional.ofNullable(this.id);
    }

    public Optional<String> getCourse( )
    {
        return Optional.ofNullable(this.course);
    }

    public Optional<String> getLecturer( )
    {
        return Optional.ofNullable(this.lecturer);
    }

    @Getter
    @AllArgsConstructor
    public enum Type
    {
        ONLINE(1), PRESENCE(2), HYBRID(3);

        private final int typeId;

        public static Type getByTypeId(int typeId)
        {
            return Arrays.stream(Type.values()).filter(type -> type.getTypeId() == typeId).findAny().orElse(null);
        }
    }
}