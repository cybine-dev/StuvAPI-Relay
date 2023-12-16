package de.cybine.stuvapi.relay.service.action.data;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.stuvapi.relay.exception.action.*;
import de.cybine.stuvapi.relay.exception.converter.*;
import io.quarkus.arc.*;
import lombok.*;
import lombok.experimental.*;
import lombok.extern.slf4j.*;

import java.nio.charset.*;
import java.util.*;

@Data
@Slf4j
@Accessors(fluent = true)
public class ActionData<T>
{
    @JsonProperty("@type")
    private final String typeName;

    @JsonProperty("value")
    private final T value;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JavaType type;

    public ActionData(String typeName, T value)
    {
        this.typeName = typeName;
        this.value = value;
    }

    public JavaType type( )
    {
        JavaType javaType = this.findType().orElse(null);
        if (javaType != null)
            return javaType;

        log.warn("Unknown action data-type '{}' found: Using default map-type", this.typeName);
        return Arc.container().select(ActionDataTypeRegistry.class).get().getDefaultType();
    }

    private Optional<JavaType> findType( )
    {
        if (this.type != null)
            return Optional.of(this.type);

        ActionDataTypeRegistry registry = Arc.container().select(ActionDataTypeRegistry.class).get();
        JavaType javaType = registry.findType(this.typeName).orElse(null);
        if (javaType != null)
        {
            this.type = javaType;
            return Optional.of(javaType);
        }

        return Optional.empty();
    }

    public boolean hasKnownType( )
    {
        return this.findType().isPresent();
    }

    public String toBase64( )
    {
        return new String(Base64.getEncoder().encode(this.toJson().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    public String toJson( )
    {
        try
        {
            ObjectMapper mapper = Arc.container().select(ObjectMapper.class).get();
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException exception)
        {
            throw new EntityConversionException("Could not serialize to base64", exception);
        }
    }

    public static <T> ActionData<T> of(T value)
    {
        TypeFactory typeFactory = Arc.container().select(ObjectMapper.class).get().getTypeFactory();
        ActionDataTypeRegistry registry = Arc.container().select(ActionDataTypeRegistry.class).get();

        JavaType type = typeFactory.constructType(value.getClass());
        String typeName = registry.findTypeName(type).orElse(null);
        if (typeName == null)
        {
            log.warn("Unknown action data-type '{}' found: Using type-name '{}'", type.getTypeName(),
                    registry.getDefaultTypeName());

            typeName = registry.getDefaultTypeName();
        }

        return new ActionData<>(typeName, value);
    }

    public static <T> ActionData<T> fromBase64(String data)
    {
        return ActionData.fromJson(new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8));

    }

    public static <T> ActionData<T> fromJson(String json)
    {
        if (json == null)
            return null;

        try
        {
            ObjectMapper mapper = Arc.container().select(ObjectMapper.class).get();
            JsonNode jsonNode = mapper.readTree(json);
            String typeName = jsonNode.findValue("@type").asText();

            ActionDataTypeRegistry registry = Arc.container().select(ActionDataTypeRegistry.class).get();
            JavaType type = registry.findType(typeName).orElse(null);
            if (type == null)
                log.warn("Unknown action data-type '{}' found: Skipping type inference for now", typeName);

            T data = mapper.treeToValue(jsonNode.findValue("value"), type);

            return new ActionData<>(typeName, data);
        }
        catch (JsonProcessingException exception)
        {
            throw new ActionProcessingException(exception);
        }
    }
}
