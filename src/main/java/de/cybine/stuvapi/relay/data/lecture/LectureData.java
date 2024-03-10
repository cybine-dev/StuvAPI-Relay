package de.cybine.stuvapi.relay.data.lecture;

import com.fasterxml.jackson.annotation.*;
import de.cybine.quarkus.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LectureData implements Serializable, WithId<Long>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private final Long id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("course")
    private final String course;

    @JsonProperty("startTime")
    private final OffsetDateTime startsAt;

    @JsonProperty("endTime")
    private final OffsetDateTime endsAt;

    @JsonProperty("type")
    private final LectureType type;

    @JsonProperty("rooms")
    private final Set<String> rooms;

    public Optional<String> getCourse( )
    {
        return Optional.ofNullable(this.course);
    }
}
