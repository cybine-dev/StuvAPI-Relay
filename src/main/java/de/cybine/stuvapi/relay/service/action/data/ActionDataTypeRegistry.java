package de.cybine.stuvapi.relay.service.action.data;

import com.fasterxml.jackson.databind.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
public class ActionDataTypeRegistry
{

    private final ObjectMapper objectMapper;

    private final Map<String, JavaType> dataTypes = new HashMap<>();

    @Getter
    private final String defaultTypeName = "unknown-type";

    @Getter
    private JavaType defaultType;

    @PostConstruct
    void setup( )
    {
        this.defaultType = this.objectMapper.getTypeFactory()
                                            .constructMapType(HashMap.class, String.class, Object.class);
    }

    public void registerType(String name, JavaType type)
    {
        if (this.defaultTypeName.equals(name))
            throw new IllegalArgumentException(
                    String.format("Cannot use name %s: Already in use for non-defined types", name));

        this.dataTypes.put(name, type);
    }

    public JavaType getType(String name)
    {
        return this.findType(name).orElse(this.defaultType);
    }

    public Optional<JavaType> findType(String name)
    {
        return Optional.ofNullable(this.dataTypes.get(name));
    }

    public String getTypeName(JavaType type)
    {
        return this.findTypeName(type).orElse(this.defaultTypeName);
    }

    public Optional<String> findTypeName(JavaType type)
    {
        return this.dataTypes.entrySet()
                             .stream()
                             .filter(entry -> entry.getValue().equals(type))
                             .map(Map.Entry::getKey)
                             .findFirst();
    }
}
