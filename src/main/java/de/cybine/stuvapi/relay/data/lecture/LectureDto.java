package de.cybine.stuvapi.relay.data.lecture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.cybine.stuvapi.relay.data.room.RoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Data
@Schema(name = "Lecture")
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
        if (this.lecturer == null || this.lecturer.isBlank())
            return Optional.empty();

        return Optional.of(this.lecturer);
    }

    public Optional<Set<RoomDto>> getRooms( )
    {
        return Optional.ofNullable(this.rooms);
    }

    @JsonIgnore
    public boolean isHoliday( )
    {
        return this.startsAt.until(this.endsAt, ChronoUnit.HOURS) >= 10 && this.rooms.isEmpty();
    }

    @JsonIgnore
    public boolean isExam( )
    {
        return Pattern.compile(".*(klausur|prüfung).*",
                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.CANON_EQ | Pattern.UNICODE_CASE)
                .matcher(this.name)
                .matches();
    }

    @JsonIgnore
    public boolean isRegularLecture( )
    {
        return !this.isExam() && !this.isHoliday();
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "LectureType")
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