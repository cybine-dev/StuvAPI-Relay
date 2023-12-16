package de.cybine.stuvapi.relay.util.converter;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.function.*;

/**
 * <p>Custom key mapping definition for converters</p>
 *
 * @param <T> type of the object to get the key from
 * @param <K> type of the key
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConverterKeyMapper<T, K>
{
    /**
     * {@link Class<T>} of the object to get the key from
     */
    @JsonProperty("type")
    private final Class<T> type;

    /**
     * Method used to extract the key
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private final Function<T, K> keyMapper;

    /**
     * <p>Method used to extract the key from a given object</p>
     *
     * @param item object to extract the key from (should not be null depending on the implementation of the mapping
     *             function)
     *
     * @return key of the object
     */
    public K getKey(T item)
    {
        return this.keyMapper.apply(item);
    }

    public static <T, K> ConverterKeyMapper<T, K> create(Class<T> type, Function<T, K> keyMapper)
    {
        return new ConverterKeyMapper<>(type, keyMapper);
    }
}
