package de.cybine.stuvapi.relay.data.lecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.cybine.stuvapi.relay.data.room.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    private final boolean isArchived;

    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final ZonedDateTime startsAt;
    private final ZonedDateTime endsAt;

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

    @JsonIgnore
    public boolean isHoliday( )
    {
        boolean beginsAtEightOClock = this.startsAt.toLocalTime().equals(LocalTime.of(7, 0));
        boolean endsAtEighteenOClock = this.endsAt.toLocalTime().equals(LocalTime.of(17, 0));

        return beginsAtEightOClock && endsAtEighteenOClock && this.rooms.isEmpty();
    }

    @JsonIgnore
    public boolean isExam( )
    {
        return this.name.toLowerCase().startsWith("klausur ");
    }

    @JsonIgnore
    public boolean isRegularLecture( )
    {
        return !this.isExam() && !this.isHoliday();
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