package de.cybine.stuvapi.relay.exception.handler;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.api.*;
import lombok.extern.slf4j.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

import java.util.*;

@Slf4j
@SuppressWarnings("unused")
public class NoSuchElementHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> toResponse(NoSuchElementException exception)
    {
        log.debug("Caught handled exception", exception);
        return ApiResponse.<Void>builder()
                          .statusCode(RestResponse.Status.NOT_FOUND.getStatusCode())
                          .error(ApiError.builder().code("element-not-found").message(exception.getMessage()).build())
                          .build()
                          .transform(ApiQueryHelper::createResponse);
    }
}
