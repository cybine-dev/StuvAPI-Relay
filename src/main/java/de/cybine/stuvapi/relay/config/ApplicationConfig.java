package de.cybine.stuvapi.relay.config;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.*;
import jakarta.validation.constraints.*;

@StaticInitSafe
@SuppressWarnings("unused")
@ConfigMapping(prefix = "application")
public interface ApplicationConfig
{
    @NotNull @NotBlank
    @WithName("base-url")
    String baseUrl( );

    @NotNull @NotBlank
    @WithName("app-id")
    String appId( );

    @NotNull @NotBlank
    @WithName("email")
    String email( );

    @NotNull @NotBlank
    @WithName("service-name")
    String serviceName( );
}
