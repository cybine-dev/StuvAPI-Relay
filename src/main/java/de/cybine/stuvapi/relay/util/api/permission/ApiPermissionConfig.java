package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiPermissionConfig
{
    @Valid
    @Singular
    @JsonProperty("type_mappings")
    private final List<ApiTypeMapping> typeMappings;

    @Valid
    @Singular
    @JsonProperty("context_mappings")
    private final List<ApiContextMapping> contextMappings;

    @Singular
    @JsonProperty("available_actions")
    private final List<String> availableActions;

    @Valid
    @Singular
    @JsonProperty("capabilities")
    private final List<ApiCapability> capabilities;

    @Valid
    @Singular
    @JsonProperty("types")
    private final List<ApiTypeConfig> types;

    @Valid
    @Singular
    @JsonProperty("contexts")
    private final List<ApiContextConfig> contexts;
}
