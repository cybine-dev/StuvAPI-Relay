package de.cybine.stuvapi.relay.data.room;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Room implements Serializable, WithId<RoomId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @JsonDeserialize(using = RoomId.Deserializer.class)
    private final RoomId id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("display_name")
    private final String displayName;

    @JsonProperty("lectures")
    @JsonView(Views.Extended.class)
    private final Set<Lecture> lectures;

    public String getDisplayName( )
    {
        return this.displayName == null ? this.name : this.displayName;
    }

    public Optional<Set<Lecture>> getLectures( )
    {
        return Optional.ofNullable(this.lectures);
    }

    @JsonProperty("lecture_ids")
    @JsonView(Views.Simple.class)
    public Optional<Set<LectureId>> getLectureIds( )
    {
        return this.getLectures().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
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
