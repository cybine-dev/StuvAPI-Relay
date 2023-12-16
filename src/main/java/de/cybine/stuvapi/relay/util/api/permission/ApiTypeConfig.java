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
public class ApiTypeConfig
{
    @NotNull
    @NotBlank
    @JsonProperty("name")
    private final String name;

    @Singular
    @JsonProperty("available_actions")
    private final List<String> availableActions;

    @Valid
    @Singular
    @JsonProperty("capabilities")
    private final List<ApiCapability> capabilities;

    @Valid
    @Singular
    @JsonProperty("fields")
    private final List<ApiFieldConfig> fields;
}
