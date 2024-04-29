package de.cybine.stuvapi.relay.exception.handler;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.api.*;
import lombok.extern.slf4j.*;
import org.hibernate.exception.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@Slf4j
@SuppressWarnings("unused")
public class ConstraintViolationExceptionHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> toResponse(ConstraintViolationException exception)
    {
        log.debug("Caught handled exception", exception);
        return ApiResponse.<Void>builder()
                          .statusCode(RestResponse.Status.CONFLICT.getStatusCode())
                          .error(ApiError.builder()
                                         .code("db-constraint-violation")
                                         .message(exception.getCause().getMessage())
                                         .build())
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }
}
