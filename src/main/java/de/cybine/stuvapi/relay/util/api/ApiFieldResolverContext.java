package de.cybine.stuvapi.relay.util.api;

import de.cybine.stuvapi.relay.util.*;
import de.cybine.stuvapi.relay.util.api.permission.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

// TODO: Add permission processing
@Data
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ApiFieldResolverContext
{
    private final String contextName;

    @Getter(AccessLevel.NONE)
    private final Function<BiTuple<Type, String>, DatasourceField> fieldSupplier;

    private final List<String>        availableActions;
    private final List<ApiCapability> capabilities;
    private final List<ApiTypeConfig> types;

    private final Map<String, Type> typeMappings;

    public Optional<DatasourceFieldPath> findField(Type responseType, String fieldName)
    {
        if (fieldName == null || fieldName.isBlank())
            return Optional.empty();

        DatasourceField field = null;
        String[] names = fieldName.split("\\.");
        DatasourceFieldPath.Generator path = DatasourceFieldPath.builder();
        for (String name : names)
        {
            BiTuple<Type, String> fieldDefinition = new BiTuple<>(responseType, name);
            if (field != null)
                fieldDefinition = new BiTuple<>(field.getFieldType(), name);

            field = this.getField(fieldDefinition);
            if (field == null)
                return Optional.empty();

            path.field(field);
        }

        return Optional.of(path.build());
    }

    private DatasourceField getField(BiTuple<Type, String> fieldDefinition)
    {
        return this.fieldSupplier.apply(fieldDefinition);
    }
}
