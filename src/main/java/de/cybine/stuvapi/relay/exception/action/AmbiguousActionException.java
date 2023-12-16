package de.cybine.stuvapi.relay.exception.action;

import de.cybine.stuvapi.relay.exception.*;
import org.jboss.resteasy.reactive.*;

@SuppressWarnings("unused")
public class AmbiguousActionException extends ServiceException
{
    public AmbiguousActionException(String message)
    {
        this(message, null);
    }

    public AmbiguousActionException(String message, Throwable cause)
    {
        super("ambiguous-action", RestResponse.Status.CONFLICT, message, cause);
    }
}
