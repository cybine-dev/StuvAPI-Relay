package de.cybine.stuvapi.relay.exception.handler;

import com.fasterxml.jackson.annotation.*;
import de.cybine.stuvapi.relay.util.api.response.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.jackson.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.server.*;

@SuppressWarnings("unused")
public class ApiConstraintViolationExceptionHandler
{
    @ServerExceptionMapper
    public RestResponse<ApiResponse<ErrorResponse>> toResponse(ConstraintViolationException exception)
    {
        return ApiResponse.<ErrorResponse>builder()
                          .status(RestResponse.Status.BAD_REQUEST)
                          .value(ErrorResponse.builder()
                                              .code("api-constraint-violation")
                                              .message("invalid request data provided")
                                              .data("violations", exception.getConstraintViolations()
                                                                           .stream()
                                                                           .map(this::createViolation)
                                                                           .toList())
                                              .build())
                          .build()
                          .toResponse();
    }

    private Violation createViolation(ConstraintViolation<?> violation)
    {
        return Violation.builder()
                        .property(violation.getPropertyPath().toString())
                        .description(violation.getMessage())
                        .build();
    }

    @Data
    @Jacksonized
    @Builder(builderClassName = "Generator")
    public static class Violation
    {
        @JsonProperty("property")
        private final String property;

        @JsonProperty("description")
        private final String description;
    }
}
