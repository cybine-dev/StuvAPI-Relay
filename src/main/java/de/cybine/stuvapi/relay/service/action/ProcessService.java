package de.cybine.stuvapi.relay.service.action;

import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.cloudevent.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;

import java.util.*;

import static de.cybine.stuvapi.relay.data.action.process.ActionProcessEntity_.*;

@Startup
@Singleton
@RequiredArgsConstructor
public class ProcessService
{
    private final GenericApiQueryService<ActionProcessEntity, ActionProcess> service = GenericApiQueryService.forType(
            ActionProcessEntity.class, ActionProcess.class);

    private final ApiFieldResolver  resolver;
    private final ConverterRegistry converterRegistry;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(ActionProcess.class)
                     .withField("id", ID)
                     .withField("event_id", EVENT_ID)
                     .withField("context_id", CONTEXT_ID)
                     .withField("context", CONTEXT)
                     .withField("status", STATUS)
                     .withField("priority", PRIORITY)
                     .withField("description", DESCRIPTION)
                     .withField("creator_id", CREATOR_ID)
                     .withField("created_at", CREATED_AT)
                     .withField("due_at", DUE_AT)
                     .withField("data", DATA);
    }

    public Optional<ActionProcess> fetchById(ActionProcessId id)
    {
        DatasourceConditionDetail<UUID> idEquals = DatasourceHelper.isEqual(ID, id.getValue());
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        return this.service.fetchSingle(DatasourceQuery.builder().condition(condition).build());
    }

    public Optional<ActionProcess> fetchByEventId(String eventId)
    {
        DatasourceConditionDetail<String> idEquals = DatasourceHelper.isEqual(EVENT_ID, eventId);
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        return this.service.fetchSingle(DatasourceQuery.builder().condition(condition).build());
    }

    public List<ActionProcess> fetchByCorrelationId(String correlationId)
    {
        DatasourceConditionDetail<String> correlationIdEquals = DatasourceHelper.isEqual(
                ActionContextEntity_.CORRELATION_ID, correlationId);
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

    public Optional<CloudEvent> fetchAsCloudEventByEventId(String eventId)
    {
        DatasourceConditionDetail<String> idEquals = DatasourceHelper.isEqual(EVENT_ID, eventId);
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        DatasourceRelationInfo contextRelation = DatasourceRelationInfo.builder()
                                                                       .property(CONTEXT.getName())
                                                                       .fetch(true)
                                                                       .build();

        DatasourceQuery query = DatasourceQuery.builder().condition(condition).relation(contextRelation).build();
        return this.service.fetchSingle(query)
                           .map(this.converterRegistry.getProcessor(ActionProcess.class, CloudEvent.class)::toItem)
                           .map(ConversionResult::result);
    }

    public List<CloudEvent> fetchAsCloudEventsByCorrelationId(String correlationId)
    {
        DatasourceConditionDetail<String> idEquals = DatasourceHelper.isEqual(ActionContextEntity_.CORRELATION_ID,
                correlationId);
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        DatasourceRelationInfo contextRelation = DatasourceRelationInfo.builder()
                                                                       .property(CONTEXT.getName())
                                                                       .fetch(true)
                                                                       .condition(condition)
                                                                       .build();

        DatasourceQuery query = DatasourceQuery.builder().relation(contextRelation).build();
        return this.converterRegistry.getProcessor(ActionProcess.class, CloudEvent.class)
                                     .toList(this.service.fetch(query))
                                     .result();
    }
}
