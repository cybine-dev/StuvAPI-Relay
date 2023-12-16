package de.cybine.stuvapi.relay.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiOrderInfo
{
    @NotNull
    @JsonProperty("property")
    private final String property;

    @Builder.Default
    @JsonProperty("priority")
    private final int priority = 100;

    @Builder.Default
    @JsonProperty("ascending")
    private final boolean isAscending = true;
}
