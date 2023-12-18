package de.cybine.stuvapi.relay.api.v1.action.context;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.api.response.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@PermitAll
@ApplicationScoped
@RequiredArgsConstructor
public class ContextResource implements ContextApi
{
    private final ContextService service;

    @Override
    public RestResponse<ApiResponse<ActionContext>> fetchById(UUID id)
    {
        return ApiResponse.<ActionContext>builder()
                          .value(this.service.fetchById(ActionContextId.of(id)).orElseThrow())
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<ActionContext>> fetchByCorrelationId(String correlationId)
    {
        return ApiResponse.<ActionContext>builder()
                          .value(this.service.fetchByCorrelationId(correlationId).orElseThrow())
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<ActionContext>>> fetch(ApiQuery query)
    {
        return ApiResponse.<List<ActionContext>>builder().value(this.service.fetch(query)).build().toResponse();
    }

    @Override
    public RestResponse<ApiResponse<ActionContext>> fetchSingle(ApiQuery query)
    {
        return ApiResponse.<ActionContext>builder()
                          .value(this.service.fetchSingle(query).orElseThrow())
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(ApiCountQuery query)
    {
        return ApiResponse.<List<ApiCountInfo>>builder().value(this.service.fetchTotal(query)).build().toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<Object>>> fetchOptions(ApiOptionQuery query)
    {
        return ApiResponse.<List<Object>>builder().value(this.service.fetchOptions(query)).build().toResponse();
    }
}
