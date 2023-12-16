package de.cybine.stuvapi.relay.exception.handler;

import de.cybine.stuvapi.relay.util.api.response.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

import java.util.*;

@SuppressWarnings("unused")
public class NoSuchElementHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<ErrorResponse>> toResponse(NoSuchElementException exception)
    {
        exception.printStackTrace();
        return ApiResponse.<ErrorResponse>builder()
                          .status(RestResponse.Status.NOT_FOUND)
                          .value(ErrorResponse.builder()
                                              .code("element-not-found")
                                              .message(exception.getMessage())
                                              .build())
                          .build()
                          .toResponse();
    }
}
