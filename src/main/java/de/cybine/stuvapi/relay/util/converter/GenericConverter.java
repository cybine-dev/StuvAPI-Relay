package de.cybine.stuvapi.relay.util.converter;

import lombok.*;

import java.util.function.*;

@Getter
@RequiredArgsConstructor
public class GenericConverter<I, O> implements Converter<I, O>
{
    private final Class<I> inputType;
    private final Class<O> outputType;

    @Getter(AccessLevel.NONE)
    private final BiFunction<I, ConversionHelper, O> conversionFunction;

    @Override
    public O convert(I input, ConversionHelper helper)
    {
        return this.conversionFunction.apply(input, helper);
    }
}
