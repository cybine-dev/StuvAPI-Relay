package de.cybine.stuvapi.relay.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.config.ConfigMapping;

@StaticInitSafe
@ConfigMapping(prefix = "api.stuv")
public interface StuvApiConfig
{
    @NotNull
    String baseUrl();
}
