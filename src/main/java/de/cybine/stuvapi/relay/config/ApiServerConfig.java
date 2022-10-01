package de.cybine.stuvapi.relay.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "server.api")
public interface ApiServerConfig
{
    @NotNull
    String baseUrl( );

    @NotNull
    String email( );

    @NotNull
    String serviceName( );
}
