package de.cybine.stuvapi.relay.data;

public class EntityConversionException extends RuntimeException
{
    public EntityConversionException( )
    {
    }

    public EntityConversionException(final String message)
    {
        super(message);
    }

    public EntityConversionException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public EntityConversionException(final Throwable cause)
    {
        super(cause);
    }

    public EntityConversionException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
