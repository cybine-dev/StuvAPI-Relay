package de.cybine.stuvapi.relay.util.converter;

import java.util.*;
import java.util.function.*;

/**
 * Enhanced {@link Function} for use with {@link Converter}
 *
 * @param <I>
 *         input data-type
 * @param <O>
 *         output data-type
 */
@FunctionalInterface
@SuppressWarnings("unused")
public interface ConverterFunction<I, O> extends Function<I, O>
{
    default O apply(Supplier<? extends I> input)
    {
        return this.apply(input.get());
    }

    default O map(Supplier<Optional<? extends I>> input)
    {
        return this.map(input.get());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default O map(Optional<? extends I> input)
    {
        return input.map(this).orElse(null);
    }

    default <T> ConverterFunction<I, T> then(Function<? super O, ? extends T> after)
    {
        Objects.requireNonNull(after);
        return (I t) -> after.apply(apply(t));
    }

    default <T> ConverterFunction<T, O> before(Function<? super T, ? extends I> before)
    {
        Objects.requireNonNull(before);
        return (T t) -> apply(before.apply(t));
    }
}
