package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
public class RBACRole
{
    @NotNull
    @JsonProperty("name")
    private final String name;

    @JsonProperty("inherited_roles")
    private final List<String> inheritedRoles;

    @NotNull
    @JsonProperty("permissions")
    private final List<String> permissions;

    public boolean hasPermission(String permission)
    {
        return this.permissions.contains(permission);
    }

    public Optional<List<String>> getInheritedRoles( )
    {
        return Optional.ofNullable(this.inheritedRoles);
    }
}
