package de.cybine.stuvapi.relay.service.action;

import lombok.*;
import lombok.experimental.*;

import java.util.*;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionProcessorResult<T>
{
    private final T data;

    @Accessors(fluent = true)
    private final boolean hasData;

    public Optional<T> getData( )
    {
        return Optional.ofNullable(this.data);
    }

    public static ActionProcessorResult<Void> empty( )
    {
        return new ActionProcessorResult<>(null, false);
    }

    public static <T> ActionProcessorResult<T> of(T data)
    {
        return new ActionProcessorResult<>(data, true);
    }
}
