package de.cybine.stuvapi.relay.exception.api;

import de.cybine.stuvapi.relay.exception.*;
import org.jboss.resteasy.reactive.*;

@SuppressWarnings("unused")
public class NotImplementedException extends ServiceException
{
    public NotImplementedException( )
    {
        super("not-implemented", RestResponse.Status.NOT_IMPLEMENTED, "This method has not been implemented yet.");
    }
}
