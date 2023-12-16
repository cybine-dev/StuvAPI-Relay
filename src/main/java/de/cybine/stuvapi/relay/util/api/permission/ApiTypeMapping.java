package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiTypeMapping
{
    @NotNull
    @NotBlank
    @JsonProperty("name")
    private final String name;

    @NotNull
    @JsonProperty("type")
    private final Class<?> type;
}
