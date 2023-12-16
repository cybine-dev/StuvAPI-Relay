package de.cybine.stuvapi.relay.data.lecture;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Lecture implements Serializable, WithId<LectureId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @JsonDeserialize(using = LectureId.Deserializer.class)
    private final LectureId id;

    @JsonProperty("lecture_id")
    private final Long lectureId;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("course")
    private final String course;

    @JsonProperty("starts_at")
    private final ZonedDateTime startsAt;

    @JsonProperty("ends_at")
    private final ZonedDateTime endsAt;

    @JsonProperty("type")
    private final LectureType type;

    @JsonProperty("status")
    private final LectureStatus status;

    @JsonProperty("rooms")
    @JsonView(Views.Extended.class)
    private final Set<Room> rooms;

    public Optional<String> getCourse( )
    {
        return Optional.ofNullable(this.course);
    }

    public Optional<Set<Room>> getRooms( )
    {
        return Optional.ofNullable(this.rooms);
    }

    @JsonProperty("room_ids")
    @JsonView(Views.Simple.class)
    public Optional<Set<RoomId>> getRoomIds( )
    {
        return this.getRooms().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
    }

    @JsonIgnore
    public boolean isHoliday( )
    {
        return this.startsAt.until(this.endsAt, ChronoUnit.HOURS) >= 10 && this.getRooms()
                                                                               .map(Set::isEmpty)
                                                                               .orElse(true);
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

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;

        if (this.getClass() != other.getClass())
            return false;

        WithId<?> that = ((WithId<?>) other);
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}