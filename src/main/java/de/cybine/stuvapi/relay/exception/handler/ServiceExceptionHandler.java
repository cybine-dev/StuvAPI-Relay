package de.cybine.stuvapi.relay.exception.handler;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.exception.*;
import de.cybine.quarkus.util.api.*;
import lombok.extern.slf4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@Slf4j
@SuppressWarnings("unused")
public class ServiceExceptionHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> toResponse(ServiceException exception)
    {
        log.debug("Caught handled exception", exception);
        return ApiResponse.<Void>builder()
                          .statusCode(exception.getStatusCode())
                          .error(exception.toResponse())
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }
}
