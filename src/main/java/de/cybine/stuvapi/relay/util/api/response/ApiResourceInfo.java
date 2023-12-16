package de.cybine.stuvapi.relay.util.api.response;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResourceInfo
{
    @JsonProperty("href")
    private final String href;

    @JsonProperty("total")
    private final Long total;

    @JsonProperty("size")
    private final Long size;

    @JsonProperty("offset")
    private final Long offset;

    @JsonProperty("resource")
    private final ApiResourceDefinition resource;

    public Optional<Long> getTotal( )
    {
        return Optional.ofNullable(this.total);
    }

    public Optional<Long> getSize( )
    {
        return Optional.ofNullable(this.size);
    }

    public Optional<Long> getOffset( )
    {
        return Optional.ofNullable(this.offset);
    }

    public Optional<ApiResourceDefinition> getResource( )
    {
        return Optional.ofNullable(this.resource);
    }
}
