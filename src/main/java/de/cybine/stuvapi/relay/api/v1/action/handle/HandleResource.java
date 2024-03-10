package de.cybine.stuvapi.relay.api.v1.action.handle;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.exception.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.api.response.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.service.action.*;
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
    public RestResponse<ApiResponse<String>> create(String namespace, String category, String name, String itemId)
    {
        return ApiResponse.<String>builder()
                          .status(RestResponse.Status.CREATED)
                          .value(this.actionService.beginWorkflow(namespace, category, name, itemId))
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<ActionProcess>> terminate(String correlationId)
    {
        return this.process(correlationId, null, ActionService.TERMINATED_STATE, null);
    }

    @Override
    public RestResponse<ApiResponse<ActionProcess>> process(String correlationId, String eventId, String action,
            Map<String, Object> data)
    {
        ActionContext context = this.contextService.fetchByCorrelationId(correlationId).orElseThrow();
        ActionProcess currentState = this.actionService.fetchCurrentState(correlationId).orElseThrow();
        if (eventId != null && !Objects.equals(currentState.getEventId(), eventId))
            return ApiResponse.<ActionProcess>builder().status(RestResponse.Status.CONFLICT).build().toResponse();

        ActionData<Object> actionData = null;
        if (data != null)
        {
            if (!data.containsKey("@type"))
                throw new IllegalArgumentException("@type must be defined when data-object is used");

            if (!data.containsKey("value"))
                throw new IllegalArgumentException("value must be defined when data-object is used");

            try
            {
                String typeName = (String) data.get("@type");
                JavaType dataType = this.typeRegistry.findType(typeName).orElseThrow();

                String serializedData = this.objectMapper.writeValueAsString(data.get("value"));
                actionData = new ActionData<>(typeName, this.objectMapper.readValue(serializedData, dataType));
            }
            catch (JsonProcessingException exception)
            {
                throw new ActionProcessingException("Could not process data", exception);
            }
        }

        ActionMetadata metadata = ActionMetadata.builder()
                                                .namespace(context.getNamespace())
                                                .category(context.getCategory())
                                                .name(context.getName())
                                                .action(action)
                                                .correlationId(correlationId)
                                                .createdAt(ZonedDateTime.now())
                                                .build();

        this.actionService.perform(Action.of(metadata, actionData));

        return ApiResponse.<ActionProcess>builder()
                          .value(this.actionService.fetchCurrentState(correlationId).orElseThrow())
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<String>>> fetchAvailableActions(String correlationId)
    {
        return ApiResponse.<List<String>>builder()
                          .value(this.actionService.availableActions(correlationId)
                                                   .stream()
                                                   .map(ActionProcessorMetadata::getAction)
                                                   .toList())
                          .build()
                          .toResponse();
    }
}
