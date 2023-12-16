package de.cybine.stuvapi.relay.exception.handler;

import de.cybine.stuvapi.relay.exception.*;
import de.cybine.stuvapi.relay.util.api.response.*;
import lombok.extern.log4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@Log4j2
@SuppressWarnings("unused")
public class ServiceExceptionHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<ErrorResponse>> toResponse(ServiceException exception)
    {
        log.debug("Caught handled exception", exception);
        return ApiResponse.<ErrorResponse>builder()
                          .status(exception.getStatus())
                          .value(exception.toResponse())
                          .build()
                          .toResponse();
    }
}
