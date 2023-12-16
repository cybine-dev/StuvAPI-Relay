package de.cybine.stuvapi.relay.service.action;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.util.api.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.cloudevent.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

import java.util.*;

import static de.cybine.stuvapi.relay.data.action.process.ActionProcessEntity_.*;

@Startup
@ApplicationScoped
@RequiredArgsConstructor
public class ProcessService
{
    private final GenericDatasourceService<ActionProcessEntity, ActionProcess> service =
            GenericDatasourceService.forType(
            ActionProcessEntity.class, ActionProcess.class);

    private final ApiFieldResolver  resolver;
    private final ConverterRegistry converterRegistry;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerTypeRepresentation(ActionProcess.class, ActionProcessEntity.class)
                     .registerField("id", ID)
                     .registerField("event_id", EVENT_ID)
                     .registerField("context_id", CONTEXT_ID)
                     .registerField("context", CONTEXT)
                     .registerField("status", STATUS)
                     .registerField("priority", PRIORITY)
                     .registerField("description", DESCRIPTION)
                     .registerField("creator_id", CREATOR_ID)
                     .registerField("created_at", CREATED_AT)
                     .registerField("due_at", DUE_AT)
                     .registerField("data", DATA);
    }

    public Optional<ActionProcess> fetchById(ActionProcessId id)
    {
        DatasourceConditionDetail<UUID> idEquals = DatasourceHelper.isEqual(ID, id.getValue());
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        return this.service.fetchSingle(DatasourceQuery.builder().condition(condition).build());
    }

    public Optional<ActionProcess> fetchByEventId(UUID eventId)
    {
        DatasourceConditionDetail<String> idEquals = DatasourceHelper.isEqual(EVENT_ID, eventId.toString());
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        return this.service.fetchSingle(DatasourceQuery.builder().condition(condition).build());
    }

    public List<ActionProcess> fetchByCorrelationId(UUID correlationId)
    {
        DatasourceConditionDetail<String> correlationIdEquals = DatasourceHelper.isEqual(
                ActionContextEntity_.CORRELATION_ID, correlationId.toString());
        DatasourceConditionInfo condition = DatasourceHelper.and(correlationIdEquals);

        DatasourceRelationInfo contextRelation = DatasourceRelationInfo.builder()
                                                                       .property(CONTEXT.getName())
                                                                       .condition(condition)
                                                                       .build();

        return this.service.fetch(DatasourceQuery.builder().relation(contextRelation).build());
    }

    public List<ActionProcess> fetch(ApiQuery query)
    {
        return this.service.fetch(query);
    }

    public Optional<ActionProcess> fetchSingle(ApiQuery query)
    {
        return this.service.fetchSingle(query);
    }

    public <O> List<O> fetchOptions(ApiOptionQuery query)
    {
        return this.service.fetchOptions(query);
    }

    public List<ApiCountInfo> fetchTotal(ApiCountQuery query)
    {
        return this.service.fetchTotal(query);
    }

    public Optional<CloudEvent> fetchAsCloudEventByEventId(UUID eventId)
    {
        DatasourceConditionDetail<String> idEquals = DatasourceHelper.isEqual(EVENT_ID, eventId.toString());
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        DatasourceRelationInfo metadataRelation = DatasourceHelper.fetch(ActionContextEntity_.METADATA);
        DatasourceRelationInfo contextRelation = DatasourceRelationInfo.builder()
                                                                       .property(CONTEXT.getName())
                                                                       .fetch(true)
                                                                       .relation(metadataRelation)
                                                                       .build();

        DatasourceQuery query = DatasourceQuery.builder().condition(condition).relation(contextRelation).build();
        return this.service.fetchSingle(query)
                           .map(this.converterRegistry.getProcessor(ActionProcess.class, CloudEvent.class)::toItem)
                           .map(ConversionResult::result);
    }

    public List<CloudEvent> fetchAsCloudEventsByCorrelationId(UUID correlationId)
    {
        DatasourceConditionDetail<String> idEquals = DatasourceHelper.isEqual(ActionContextEntity_.CORRELATION_ID,
                correlationId.toString());
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        DatasourceRelationInfo metadataRelation = DatasourceHelper.fetch(ActionContextEntity_.METADATA);
        DatasourceRelationInfo contextRelation = DatasourceRelationInfo.builder()
                                                                       .property(CONTEXT.getName())
                                                                       .fetch(true)
                                                                       .condition(condition)
                                                                       .relation(metadataRelation)
                                                                       .build();

        DatasourceQuery query = DatasourceQuery.builder().relation(contextRelation).build();
        return this.converterRegistry.getProcessor(ActionProcess.class, CloudEvent.class)
                                     .toList(this.service.fetch(query))
                                     .result();
    }
}
