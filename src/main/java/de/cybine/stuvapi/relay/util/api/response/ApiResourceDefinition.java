package de.cybine.stuvapi.relay.util.api.response;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResourceDefinition
{
    @JsonProperty("href")
    private final String href;
}
