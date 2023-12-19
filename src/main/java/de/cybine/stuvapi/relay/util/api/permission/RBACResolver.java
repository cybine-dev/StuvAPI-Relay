package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.exception.*;
import de.cybine.stuvapi.relay.util.*;
import io.quarkus.arc.*;
import io.quarkus.security.identity.*;
import io.smallrye.mutiny.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
public class RBACResolver
{
    private final ObjectMapper      objectMapper;
    private final ApplicationConfig applicationConfig;

    private final Map<String, RBACRole> roles = new HashMap<>();

    @PostConstruct
    void setup( ) throws URISyntaxException
    {
        FilePathHelper.resolvePath(this.applicationConfig.paths().rbacPath()).ifPresent(this::reload);
    }

    public void reload(Path path)
    {
        try
        {
            JavaType type = this.objectMapper.getTypeFactory().constructParametricType(List.class, RBACRole.class);
            List<RBACRole> rbacRoles = this.objectMapper.readValue(path.toFile(), type);

            this.roles.clear();
            for (RBACRole role : rbacRoles)
                this.roles.put(role.getName(), role);
        }
        catch (IOException exception)
        {
            throw new TechnicalException("Could not load rbac-data", exception);
        }
    }

    public boolean hasPermission(String role, Permission permission)
    {
        RBACRole rbacRole = this.roles.get(role);
        if (rbacRole == null)
            return false;

        if (rbacRole.hasPermission(permission.getName()))
            return true;

        if (rbacRole.getInheritedRoles().isEmpty())
            return false;

        return rbacRole.getInheritedRoles()
                       .orElseThrow()
                       .stream()
                       .map(this.roles::get)
                       .filter(Objects::nonNull)
                       .anyMatch(item -> item.hasPermission(permission.getName()));
    }

    public PermissionChecker toPermissionChecker( )
    {
        SecurityIdentity securityIdentity = Arc.container().select(SecurityIdentity.class).get();
        return permission -> Uni.createFrom()
                                .item(securityIdentity.getRoles()
                                                      .stream()
                                                      .anyMatch(item -> this.hasPermission(item, permission)));
    }
}
