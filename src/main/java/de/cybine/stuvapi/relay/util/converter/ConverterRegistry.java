package de.cybine.stuvapi.relay.util.converter;

import jakarta.enterprise.context.*;

import java.util.*;

@ApplicationScoped
@SuppressWarnings("unused")
public class ConverterRegistry
{
    private final Map<ConverterType<?, ?>, Converter<?, ?>> converters = new HashMap<>();

    /**
     * Register a converter
     *
     * @param converter
     *         converter to register
     */
    public void addConverter(Converter<?, ?> converter)
    {
        this.converters.put(converter.getType(), converter);
    }

    /**
     * Register both converters of an {@link EntityMapper}
     *
     * @param mapper
     *         mapper to register
     */
    public void addEntityMapper(EntityMapper<?, ?> mapper)
    {
        this.addConverter(mapper.toDataConverter());
        this.addConverter(mapper.toEntityConverter());
    }

    /**
     * Creates a helper object for converting item(s) of the given input type {@link I} to the given output type
     * {@link O} with default conversion settings
     *
     * @param inputType
     *         class of input data-type
     * @param outputType
     *         class of output data-type
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return helper object for converting item(s)
     */
    public <I, O> ConversionProcessor<I, O> getProcessor(Class<I> inputType, Class<O> outputType)
    {
        return this.getProcessor(inputType, outputType, ConverterTree.create());
    }

    /**
     * Creates a helper object for converting item(s) of the given input type {@link I} to the given output type
     * {@link O} with custom conversion settings
     *
     * @param inputType
     *         class of input data-type
     * @param outputType
     *         class of output data-type
     * @param <I>
     *         input data-type
     * @param <O>
     *         output data-type
     *
     * @return helper object for converting item(s)
     */
    public <I, O> ConversionProcessor<I, O> getProcessor(Class<I> inputType, Class<O> outputType,
            ConverterTree metadata)
    {
        return new ConversionProcessor<>(inputType, outputType, metadata, this::getConverter);
    }

    @SuppressWarnings("unchecked")
    private <I, O> Converter<I, O> getConverter(ConverterType<I, O> type)
    {
        return (Converter<I, O>) this.converters.get(type);
    }
}
