package de.cybine.stuvapi.relay.config;

import de.cybine.stuvapi.relay.exception.api.*;
import de.cybine.stuvapi.relay.util.api.permission.*;
import io.netty.handler.codec.http.*;
import io.quarkus.oidc.runtime.*;
import io.quarkus.security.identity.*;
import io.quarkus.security.identity.request.*;
import io.quarkus.vertx.http.runtime.security.*;
import io.smallrye.mutiny.*;
import io.vertx.ext.web.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import lombok.*;

import java.util.*;

@Priority(1)
@Alternative
@ApplicationScoped
@RequiredArgsConstructor
public class AuthenticationConfig implements HttpAuthenticationMechanism, SecurityIdentityAugmentor
{
    public static final Mechanism DEFAULT_MECHANISM = Mechanism.OIDC;

    private final RBACResolver rbacResolver;

    private final OidcAuthenticationMechanism oidcMechanism;

    private final Instance<SecurityIdentityConfig> securityIdentityConfig;

    private Mechanism mechanism;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager)
    {
        return this.selectMechanism(context).authenticate(context, identityProviderManager);
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context)
    {
        return this.selectMechanism(context).getCredentialTransport(context);
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context)
    {
        return this.oidcMechanism.getChallenge(context);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes( )
    {
        Set<Class<? extends AuthenticationRequest>> types = new HashSet<>();
        types.addAll(this.oidcMechanism.getCredentialTypes());

        return types;
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context)
    {
        PermissionChecker permissionChecker = this.rbacResolver.toPermissionChecker();
        return context.runBlocking(this.securityIdentityConfig.get()
                                                              .mechanism(this.mechanism)
                                                              .permissionChecker(permissionChecker)
                                                              .identity(identity));
    }

    private HttpAuthenticationMechanism selectMechanism(RoutingContext context)
    {
        String authHeader = context.request().getHeader(HttpHeaderNames.AUTHORIZATION);
        this.mechanism = this.getMechanism(authHeader);
        return switch (this.mechanism)
        {
            case API_TOKEN -> throw new NotImplementedException();
            case OIDC -> this.oidcMechanism;
        };
    }

    private Mechanism getMechanism(String authorizationHeader)
    {
        if (authorizationHeader != null && !authorizationHeader.isBlank())
        {
            String authType = authorizationHeader.split(" ")[ 0 ].toLowerCase();
            return Mechanism.findByName(authType).orElse(AuthenticationConfig.DEFAULT_MECHANISM);
        }

        return AuthenticationConfig.DEFAULT_MECHANISM;
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Mechanism
    {
        OIDC("bearer"), API_TOKEN("token");

        private final String name;

        public static Optional<Mechanism> findByName(String name)
        {
            if (name == null)
                return Optional.empty();

            return Arrays.stream(Mechanism.values()).filter(item -> item.getName().equals(name)).findAny();
        }
    }
}
