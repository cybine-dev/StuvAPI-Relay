package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiFieldConfig
{
    @NotNull
    @NotBlank
    @JsonProperty("name")
    private final String name;

    @Builder.Default
    @JsonProperty("is_available")
    private final boolean isAvailable = true;

    @Valid
    @Singular
    @JsonProperty("capabilities")
    private final List<ApiCapability> capabilities;
}
