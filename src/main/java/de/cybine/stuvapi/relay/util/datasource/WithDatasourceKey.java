package de.cybine.stuvapi.relay.util.datasource;

/**
 * <p>API-specific interface to map a given input value to another datasource value</p>
 * <p>Can be used to specify a field of an enum as the actual datasource value for searching</p>
 *
 * @param <T>
 *         type of the value
 */
public interface WithDatasourceKey<T>
{
    /**
     * @return actual datasource value
     */
    T getDatasourceKey( );
}
