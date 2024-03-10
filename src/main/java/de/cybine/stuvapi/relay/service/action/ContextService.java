package de.cybine.stuvapi.relay.service.action;

import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.inject.*;
import lombok.*;

import java.util.*;

import static de.cybine.stuvapi.relay.data.action.context.ActionContextEntity_.*;

@Startup
@Singleton
@RequiredArgsConstructor
public class ContextService
{
    private final GenericApiQueryService<ActionContextEntity, ActionContext> service = GenericApiQueryService.forType(
            ActionContextEntity.class, ActionContext.class);

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(ActionContext.class)
                     .withField("id", ID)
                     .withField("namespace", NAMESPACE)
                     .withField("category", CATEGORY)
                     .withField("name", NAME)
                     .withField("correlation_id", CORRELATION_ID)
                     .withField("item_id", ITEM_ID)
                     .withRelation("processes", PROCESSES, ActionProcess.class);
    }

    public Optional<ActionContext> fetchById(ActionContextId id)
    {
        DatasourceConditionDetail<UUID> idEquals = DatasourceHelper.isEqual(ID, id.getValue());
        DatasourceConditionInfo condition = DatasourceHelper.and(idEquals);

        return this.service.fetchSingle(DatasourceQuery.builder().condition(condition).build());
    }

    public Optional<ActionContext> fetchByCorrelationId(String correlationId)
    {
        DatasourceConditionDetail<String> correlationIdEquals = DatasourceHelper.isEqual(CORRELATION_ID, correlationId);
        DatasourceConditionInfo condition = DatasourceHelper.and(correlationIdEquals);

        return this.service.fetchSingle(DatasourceQuery.builder().condition(condition).build());
    }

    public List<ActionContext> fetch(ApiQuery query)
    {
        return this.service.fetch(query);
    }

    public Optional<ActionContext> fetchSingle(ApiQuery query)
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
}
