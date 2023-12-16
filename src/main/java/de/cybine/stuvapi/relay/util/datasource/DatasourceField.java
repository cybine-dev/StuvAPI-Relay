package de.cybine.stuvapi.relay.util.datasource;

import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.exception.*;
import io.quarkus.arc.*;
import jakarta.persistence.*;
import lombok.*;

import java.lang.reflect.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceField
{
    private final String name;

    private final Type objectType;
    private final Type fieldType;

    private final JavaType itemType;

    private final boolean isIterable;
    private final boolean isRelation;

    public static DatasourceField property(Class<?> objectType, String name, Class<?> fieldType)
    {
        try
        {
            Field field = objectType.getDeclaredField(name);

            return DatasourceField.builder()
                                  .name(name)
                                  .objectType(objectType)
                                  .fieldType(fieldType)
                                  .itemType(DatasourceField.createType(fieldType))
                                  .isIterable(Iterable.class.isAssignableFrom(field.getType()))
                                  .isRelation(field.isAnnotationPresent(Entity.class))
                                  .build();
        }
        catch (NoSuchFieldException exception)
        {
            throw new TechnicalException("Invalid entity field definition", exception);
        }
    }

    private static JavaType createType(Type type)
    {
        return Arc.container().select(ObjectMapper.class).get().constructType(type);
    }
}
