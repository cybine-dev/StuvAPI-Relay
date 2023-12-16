package de.cybine.stuvapi.relay.exception.action;

import de.cybine.stuvapi.relay.exception.*;
import org.jboss.resteasy.reactive.*;

@SuppressWarnings("unused")
public class UnknownActionException extends ServiceException
{
    public UnknownActionException(String message)
    {
        this(message, null);
    }

    public UnknownActionException(String message, Throwable cause)
    {
        super("unknown-action", RestResponse.Status.BAD_REQUEST, message, cause);
    }
}
