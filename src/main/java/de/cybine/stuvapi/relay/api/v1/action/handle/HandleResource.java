package de.cybine.stuvapi.relay.api.v1.action.handle;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.exception.action.*;
import de.cybine.stuvapi.relay.service.action.*;
import de.cybine.stuvapi.relay.service.action.data.*;
import de.cybine.stuvapi.relay.util.api.response.*;
import io.quarkus.security.*;
import jakarta.enterprise.context.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.time.*;
import java.util.*;

@Authenticated
@ApplicationScoped
@RequiredArgsConstructor
public class HandleResource implements HandleApi
{
    private final ObjectMapper objectMapper;

    private final ActionService  actionService;
    private final ContextService contextService;

    private final ActionDataTypeRegistry typeRegistry;

    @Override
    public RestResponse<ApiResponse<ActionContext>> create(String namespace, String category, String name,
            String itemId)
    {
        ActionContextMetadata metadata = ActionContextMetadata.builder()
                                                              .namespace(namespace)
                                                              .category(category)
                                                              .name(name)
                                                              .itemId(itemId)
                                                              .build();

        return ApiResponse.<ActionContext>builder()
                          .status(RestResponse.Status.CREATED)
                          .value(this.actionService.createContext(metadata))
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<Void>> terminate(String correlationId)
    {
        ActionContext context = this.contextService.fetchByCorrelationId(correlationId).orElseThrow();
        this.actionService.terminateContext(context.getId());

        return ApiResponse.<Void>builder().build().toResponse();
    }

    @Override
    public RestResponse<ApiResponse<ActionProcess>> process(String correlationId, String eventId, String action,
            Map<String, Object> data)
    {
        ActionContext context = this.contextService.fetchByCorrelationId(correlationId).orElseThrow();
        ActionProcess currentState = this.actionService.fetchCurrentState(context.getId()).orElseThrow();
        if (eventId != null && !Objects.equals(currentState.getEventId(), eventId))
            return ApiResponse.<ActionProcess>builder().status(RestResponse.Status.CONFLICT).build().toResponse();

        ActionData<?> actionData = null;
        if (data != null)
        {
            if (!data.containsKey("@type"))
                throw new IllegalArgumentException("@type must be defined when data-object is used");

            if (!data.containsKey("value"))
                throw new IllegalArgumentException("value must be defined when data-object is used");

            try
            {
                JavaType dataType = this.typeRegistry.findType((String) data.get("@type")).orElseThrow();

                String serializedData = this.objectMapper.writeValueAsString(data.get("value"));
                actionData = ActionData.of(this.objectMapper.readValue(serializedData, dataType));
            }
            catch (JsonProcessingException exception)
            {
                throw new ActionProcessingException("Could not process data", exception);
            }
        }

        this.actionService.process(ActionProcessMetadata.builder()
                                                        .contextId(context.getId())
                                                        .status(action)
                                                        .createdAt(ZonedDateTime.now())
                                                        .data(actionData)
                                                        .build());

        return ApiResponse.<ActionProcess>builder()
                          .value(this.actionService.fetchCurrentState(context.getId()).orElseThrow())
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<String>>> fetchAvailableActions(String correlationId)
    {
        return ApiResponse.<List<String>>builder()
                          .value(this.actionService.fetchAvailableActions(correlationId))
                          .build()
                          .toResponse();
    }
}
