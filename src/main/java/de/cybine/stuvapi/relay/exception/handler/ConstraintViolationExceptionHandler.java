package de.cybine.stuvapi.relay.exception.handler;

import de.cybine.stuvapi.relay.util.api.response.*;
import org.hibernate.exception.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@SuppressWarnings("unused")
public class ConstraintViolationExceptionHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<ErrorResponse>> toResponse(ConstraintViolationException exception)
    {
        return ApiResponse.<ErrorResponse>builder()
                          .status(RestResponse.Status.CONFLICT)
                          .value(ErrorResponse.builder()
                                              .code("db-constraint-violation")
                                              .message(exception.getCause().getMessage())
                                              .build())
                          .build()
                          .toResponse();
    }
}
