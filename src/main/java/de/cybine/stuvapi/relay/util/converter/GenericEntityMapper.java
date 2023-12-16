package de.cybine.stuvapi.relay.util.converter;

import lombok.*;

@Getter
@AllArgsConstructor
public class GenericEntityMapper<E, D> implements EntityMapper<E, D>
{
    private final Class<E> entityType;
    private final Class<D> dataType;

    @Getter(AccessLevel.NONE)
    private final Converter<E, D> entityConverter;

    @Getter(AccessLevel.NONE)
    private final Converter<D, E> dataConverter;

    @Override
    public E toEntity(D data, ConversionHelper helper)
    {
        return this.dataConverter.convert(data, helper);
    }

    @Override
    public D toData(E entity, ConversionHelper helper)
    {
        return this.entityConverter.convert(entity, helper);
    }
}
