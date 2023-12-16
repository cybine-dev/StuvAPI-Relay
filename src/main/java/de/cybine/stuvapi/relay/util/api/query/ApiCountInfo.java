package de.cybine.stuvapi.relay.util.api.query;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiCountInfo
{
    @JsonProperty("key")
    private final List<?> groupKey;

    @JsonProperty("count")
    private final long count;
}
