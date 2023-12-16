package de.cybine.stuvapi.relay.util.api;

import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.util.*;
import de.cybine.stuvapi.relay.util.api.permission.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import io.quarkus.security.identity.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ApiFieldResolver
{
    public static final String DEFAULT_CONTEXT = "default";

    private final ObjectMapper      objectMapper;
    private final SecurityIdentity  securityIdentity;
    private final ApplicationConfig applicationConfig;

    private final Map<String, ApiFieldResolverContext> contexts = new HashMap<>();

    private final Map<BiTuple<Type, String>, DatasourceField> fields = new HashMap<>();

    private final Map<Type, Type> representationTypes = new HashMap<>();

    private ApiPermissionConfig permissionConfig;

    @PostConstruct
    void setup( ) throws URISyntaxException, IOException
    {
        this.permissionConfig = this.objectMapper.readValue(
                FilePathHelper.resolvePath(this.applicationConfig.paths().apiPermissionsPath()).toFile(),
                ApiPermissionConfig.class);

        this.contexts.clear();
        for (ApiContextConfig context : this.permissionConfig.getContexts())
            System.out.println("ctx: " + this.getContext(context.getName()).getContextName());
    }

    public ApiFieldResolverContext getUserContext( )
    {
        return this.permissionConfig.getContextMappings()
                                    .stream()
                                    .filter(item -> this.securityIdentity.checkPermissionBlocking(
                                            ApiPermission.of(item.getPermission())))
                                    .sorted()
                                    .findFirst()
                                    .map(ApiContextMapping::getName)
                                    .flatMap(this::findContext)
                                    .orElseGet(this::getDefaultContext);
    }

    public ApiFieldResolverContext getDefaultContext( )
    {
        return this.getContext(ApiFieldResolver.DEFAULT_CONTEXT);
    }

    public ApiFieldResolverContext getContext(String name)
    {
        return this.contexts.computeIfAbsent(name,
                context -> new ApiFieldResolverContext(context, this.fields::get, new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(), new HashMap<>()));
    }

    public Optional<ApiFieldResolverContext> findContext(String context)
    {
        return Optional.ofNullable(this.contexts.get(context));
    }

    public ApiFieldResolver registerField(Type dataType, String alias, DatasourceField field)
    {
        log.debug("Registering api-field: {}({})", dataType.getTypeName(), alias);

        this.fields.put(new BiTuple<>(this.findRepresentationType(dataType).orElse(dataType), alias), field);
        return this;
    }

    public ApiFieldResolverHelper registerTypeRepresentation(Type representationType, Type datasourceType)
    {
        this.representationTypes.put(representationType, datasourceType);
        return this.getTypeRepresentationHelper(representationType);
    }

    public ApiFieldResolverHelper getTypeRepresentationHelper(Type representationType)
    {
        return new ApiFieldResolverHelper(this, representationType);
    }

    public Optional<Type> findRepresentationType(Type type)
    {
        return Optional.ofNullable(this.representationTypes.get(type));
    }
}
