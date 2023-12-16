package de.cybine.stuvapi.relay.util.converter;

/**
 * Helper class to define method to retrieve {@link Converter}
 */
public interface ConverterResolver
{
    <I, O> Converter<I, O> getConverter(ConverterType<I, O> type);

    default <I, O> Converter<I, O> getConverter(Class<I> inputType, Class<O> outputType)
    {
        return this.getConverter(new ConverterType<>(inputType, outputType));
    }
}
