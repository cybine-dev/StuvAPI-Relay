package de.cybine.stuvapi.relay.exception;

@SuppressWarnings("unused")
public class StuvApiFetchException extends RuntimeException
{
    public StuvApiFetchException( )
    {
    }

    public StuvApiFetchException(final String message)
    {
        super(message);
    }

    public StuvApiFetchException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public StuvApiFetchException(final Throwable cause)
    {
        super(cause);
    }

    public StuvApiFetchException(final String message, final Throwable cause, final boolean enableSuppression,
            final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
