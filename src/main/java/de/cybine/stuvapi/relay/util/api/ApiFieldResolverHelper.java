package de.cybine.stuvapi.relay.util.api;

import de.cybine.stuvapi.relay.util.datasource.*;
import lombok.*;

import java.lang.reflect.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ApiFieldResolverHelper
{
    private final ApiFieldResolver resolver;

    private final Type dataType;

    public ApiFieldResolverHelper registerField(String alias, DatasourceField field)
    {
        this.resolver.registerField(this.dataType, alias, field);
        return this;
    }
}
