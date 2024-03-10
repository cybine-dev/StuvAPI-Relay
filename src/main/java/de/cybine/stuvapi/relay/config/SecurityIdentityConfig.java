package de.cybine.stuvapi.relay.config;

import de.cybine.quarkus.util.api.permission.*;
import de.cybine.stuvapi.relay.config.AuthenticationConfig.*;
import io.quarkus.security.identity.*;
import io.quarkus.security.runtime.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.context.control.*;
import lombok.*;
import lombok.experimental.*;
import lombok.extern.slf4j.*;

import java.util.function.*;

@Slf4j
@Setter
@Dependent
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class SecurityIdentityConfig implements Supplier<SecurityIdentity>
{
    private SecurityIdentity identity;

    private Mechanism         mechanism;
    private PermissionChecker permissionChecker;

    @Override
    @ActivateRequestContext
    public SecurityIdentity get( )
    {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(this.identity)
                                                                         .addPermissionChecker(this.permissionChecker);

        if (this.identity.isAnonymous())
            return builder.build();

        return switch (this.mechanism)
        {
            case API_TOKEN ->
            {
                log.debug("Augmenting api-token");
                // TODO: custom api-token logic
                yield builder.build();
            }

            case OIDC -> builder.build();
        };
    }
}
