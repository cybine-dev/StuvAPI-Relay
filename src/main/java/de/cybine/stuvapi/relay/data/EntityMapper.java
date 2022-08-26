package de.cybine.stuvapi.relay.data;

public interface EntityMapper<E, D>
{
    E toEntity(D data);

    D toData(E entity);
}
