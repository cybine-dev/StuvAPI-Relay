package de.cybine.stuvapi.relay.util.converter;

import de.cybine.stuvapi.relay.util.*;
import lombok.*;

import java.util.*;
import java.util.stream.*;

/**
 * Helper class to initiate item conversion
 *
 * @param <I>
 *         input data-type
 * @param <O>
 *         output data-type
 *
 * @see ConversionHelper
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ConversionProcessor<I, O>
{
    private final Class<I> inputType;
    private final Class<O> outputType;

    private final ConverterTree metadata;

    private final ConverterResolver converterResolver;

    private final List<BiTuple<String, Object>> context = new ArrayList<>();

    /**
     * Add copntext information
     *
     * @param property
     *         name of the context information
     * @param value
     *         value of the context information
     *
     * @return current processor instance
     */
    public ConversionProcessor<I, O> withContext(String property, Object value)
    {
        this.context.add(new BiTuple<>(property, value));
        return this;
    }

    /**
     * Convert a single item
     *
     * @param input
     *         item to convert
     *
     * @return {@link ConversionResult}
     *
     * @see ConversionHelper#toItem(Class, Class)
     */
    public ConversionResult<O> toItem(I input)
    {
        ConversionHelper helper = this.createConversionHelper();
        return new ConversionResult<>(this.metadata, helper.toItem(this.inputType, this.outputType).apply(input));
    }

    /**
     * Convert a collection of items
     *
     * @param input
     *         collection of items to convert
     *
     * @return {@link ConversionResult}
     *
     * @see ConversionProcessor#toCollection(Collection, Collection, Collector)
     * @see ConversionHelper#toList(Class, Class)
     */
    public ConversionResult<List<O>> toList(Collection<I> input)
    {
        ConversionHelper helper = this.createConversionHelper();
        return new ConversionResult<>(this.metadata, helper.toList(this.inputType, this.outputType).apply(input));
    }

    /**
     * Convert a collection of items
     *
     * @param input
     *         collection of items to convert
     *
     * @return {@link ConversionResult}
     *
     * @see ConversionProcessor#toCollection(Collection, Collection, Collector)
     * @see ConversionHelper#toSet(Class, Class)
     */
    public ConversionResult<Set<O>> toSet(Collection<I> input)
    {
        ConversionHelper helper = this.createConversionHelper();
        return new ConversionResult<>(this.metadata, helper.toSet(this.inputType, this.outputType).apply(input));
    }

    /**
     * Convert a collection of items
     *
     * @param input
     *         collection of items to convert
     * @param defaultValue
     *         default value to return if constraints are not met
     * @param collector
     *         collector for the stream of converted items
     * @param <C>
     *         output collection-type
     *
     * @return {@link ConversionResult}
     */
    public <C extends Collection<O>> ConversionResult<C> toCollection(Collection<I> input, C defaultValue,
            Collector<O, ?, C> collector)
    {
        ConversionHelper helper = this.createConversionHelper();
        return new ConversionResult<>(this.metadata,
                helper.toCollection(this.inputType, this.outputType, defaultValue, collector).apply(input));
    }

    private ConversionHelper createConversionHelper( )
    {
        ConversionHelper helper = new ConversionHelper(this.metadata.getRootNode(), this.converterResolver);
        this.context.forEach(item -> helper.withContext(item.first(), item.second()));

        return helper;
    }
}
