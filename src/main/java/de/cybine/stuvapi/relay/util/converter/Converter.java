package de.cybine.stuvapi.relay.util.converter;

/**
 * <p>Interface to define object mappings</p>
 *
 * @param <I>
 *         type of the object to be mapped
 * @param <O>
 *         type of the object that is generated in the mapping process
 */
public interface Converter<I, O>
{
    /**
     * @return type of the object to be mapped
     */
    Class<I> getInputType( );

    /**
     * @return type of the object that is generated in the mapping process
     */
    Class<O> getOutputType( );

    /**
     * @return compound type of the converter
     */
    default ConverterType<I, O> getType( )
    {
        return new ConverterType<>(this.getInputType(), this.getOutputType());
    }

    /**
     * <p>Method to perform object mapping</p>
     *
     * @param input
     *         object to be mapped
     * @param helper
     *         helper for mapping of related objects
     *
     * @return mapped object
     */
    O convert(I input, ConversionHelper helper);
}
