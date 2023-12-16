package de.cybine.stuvapi.relay.service.action;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.exception.action.*;
import de.cybine.stuvapi.relay.util.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import io.quarkus.runtime.*;
import jakarta.enterprise.context.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import lombok.extern.log4j.*;
import org.hibernate.*;

import java.time.*;
import java.util.*;

@Log4j2
@Startup
@ApplicationScoped
@RequiredArgsConstructor
public class ActionService
{
    private final ConverterRegistry       converterRegistry;
    private final ActionProcessorRegistry processorRegistry;

    private final ContextService  contextService;
    private final MetadataService metadataService;

    private final SessionFactory sessionFactory;

    private final SecurityContext securityContext;

    public ActionContext createContext(ActionContextMetadata contextMetadata)
    {
        ActionMetadata metadata = this.fetchMetadata(contextMetadata).orElse(null);
        if (metadata == null)
            throw new NoSuchElementException("unknown metadata");

        if (contextMetadata.getItemId().isPresent() && !this.fetchActiveContexts(contextMetadata).isEmpty())
            throw new AmbiguousActionException(
                    String.format("Action ns(%s) cat(%s) name(%s) already active for item-id %s",
                            contextMetadata.getNamespace(), contextMetadata.getCategory(), contextMetadata.getName(),
                            contextMetadata.getItemId().orElseThrow()));

        ConverterTree converterTree = ConverterTree.create();
        ActionContext context = ActionContext.builder()
                                             .id(ActionContextId.create())
                                             .metadata(metadata)
                                             .itemId(contextMetadata.getItemId().orElse(null))
                                             .build();

        ActionContextEntity entity = this.converterRegistry.getProcessor(ActionContext.class, ActionContextEntity.class,
                converterTree).toItem(context).result();

        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();
            transaction.begin();

            session.persist(entity);
            session.flush();

            ActionContextId contextId = ActionContextId.of(entity.getId());
            ActionProcess process = ActionProcess.builder()
                                                 .id(ActionProcessId.create())
                                                 .context(ActionContext.builder().id(contextId).build())
                                                 .status(BaseActionProcessStatus.INITIALIZED.getName())
                                                 .creatorId(this.getIdentityName().orElse(null))
                                                 .createdAt(ZonedDateTime.now())
                                                 .build();

            session.persist(this.converterRegistry.getProcessor(ActionProcess.class, ActionProcessEntity.class)
                                                  .toItem(process)
                                                  .result());

            transaction.commit();
        }

        return this.converterRegistry.getProcessor(ActionContextEntity.class, ActionContext.class)
                                     .toItem(entity)
                                     .result();
    }

    public void updateItemId(ActionContextId contextId, String itemId)
    {
        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            ActionContextEntity entity = session.byId(ActionContextEntity.class).load(contextId.getValue());
            if (entity.getItemId().isPresent())
                throw new ActionProcessingException("Cannot modify item_id of context when already set");

            entity.setItemId(itemId);

            transaction.commit();
        }
    }

    public <T> ActionProcessorResult<T> terminateContext(ActionContextId contextId)
    {
        return this.process(ActionProcessMetadata.builder()
                                                 .contextId(contextId)
                                                 .status(BaseActionProcessStatus.TERMINATED.getName())
                                                 .createdAt(ZonedDateTime.now())
                                                 .build());
    }

    public void bulkProcess(List<ActionProcessMetadata> states, boolean ignorePreconditionErrors)
    {
        if (states == null || states.isEmpty())
            return;

        ActionProcessMetadata nextState = states.get(0);
        ActionProcess state = this.fetchCurrentState(nextState.getContextId()).orElse(null);
        if (state == null)
            throw new UnknownActionStateException(
                    "Could not find action-state for context-id " + nextState.getContextId().getValue());

        ActionContext context = this.contextService.fetchById(nextState.getContextId()).orElseThrow();
        ActionMetadata metadata = this.metadataService.fetchById(context.getMetadataId()).orElseThrow();

        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            log.debug("Started processing bulk-action... [ns({}) cat({}) name({})]", metadata.getNamespace(),
                    metadata.getCategory(), metadata.getName());

            long counter = 0;
            ActionProcess currentState = state;
            for (ActionProcessMetadata processMetadata : states)
            {
                try
                {
                    currentState = this.$process(processMetadata, currentState, metadata).first();
                    if (++counter % 250 == 0)
                    {
                        log.debug("Processed {} actions [ns({}) cat({}) name({})]", counter,
                                metadata.getNamespace(), metadata.getCategory(), metadata.getName());

                        session.flush();
                        session.clear();
                    }
                }
                catch (ActionPreconditionException exception)
                {
                    if (!ignorePreconditionErrors)
                        throw exception;
                }
            }

            log.debug("Committing bulk-action... [ns({}) cat({}) name({})]", metadata.getNamespace(),
                    metadata.getCategory(), metadata.getName());

            transaction.commit();

            log.debug("Finished processing bulk-action. [ns({}) cat({}) name({})]", metadata.getNamespace(),
                    metadata.getCategory(), metadata.getName());
        }
    }

    public <T> T process(ActionProcessMetadata nextState)
    {
        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            ActionProcess state = this.fetchCurrentState(nextState.getContextId()).orElse(null);
            if (state == null)
                throw new UnknownActionStateException(
                        "Could not find action-state for context-id " + nextState.getContextId().getValue());

            ActionContext context = this.contextService.fetchById(nextState.getContextId()).orElseThrow();
            ActionMetadata metadata = this.metadataService.fetchById(context.getMetadataId()).orElseThrow();

            T result = this.<T>$process(nextState, state, metadata).second();

            transaction.commit();

            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> BiTuple<ActionProcess, T> $process(ActionProcessMetadata nextState, ActionProcess currentState,
            ActionMetadata metadata)
    {
        ActionProcessorMetadata processorMetadata = ActionProcessorMetadata.builder()
                                                                           .namespace(metadata.getNamespace())
                                                                           .category(metadata.getCategory())
                                                                           .name(metadata.getName())
                                                                           .fromStatus(currentState.getStatus())
                                                                           .toStatus(nextState.getStatus())
                                                                           .build();

        ActionProcessor<?> processor = this.processorRegistry.findProcessor(processorMetadata).orElse(null);
        if (processor == null)
            throw new UnknownActionException("Unknown action " + processorMetadata.asString());

        ActionStateTransition transition = ActionStateTransition.builder()
                                                                .service(this)
                                                                .previousState(currentState)
                                                                .nextState(nextState)
                                                                .build();

        if (!processor.shouldExecute(transition))
            throw new ActionPreconditionException(null);

        ActionContextId contextId = ActionContextId.of(nextState.getContextId().getValue());
        ActionProcess process = ActionProcess.builder()
                                             .id(ActionProcessId.create())
                                             .context(ActionContext.builder().id(contextId).build())
                                             .status(nextState.getStatus())
                                             .priority(nextState.getPriority().orElse(100))
                                             .description(nextState.getDescription().orElse(null))
                                             .creatorId(this.getIdentityName().orElse(null))
                                             .createdAt(nextState.getCreatedAt())
                                             .dueAt(nextState.getDueAt().orElse(null))
                                             .data(nextState.getData().orElse(null))
                                             .build();

        Session session = this.sessionFactory.getCurrentSession();
        session.persist(this.converterRegistry.getProcessor(ActionProcess.class, ActionProcessEntity.class)
                                              .toItem(process)
                                              .result());

        return new BiTuple<>(process, (T) processor.process(transition));
    }

    public Optional<ActionMetadata> fetchMetadata(ActionContextMetadata context)
    {
        DatasourceQueryInterpreter<ActionMetadataEntity> interpreter = DatasourceQueryInterpreter.of(
                ActionMetadataEntity.class,
                DatasourceQuery.builder().condition(this.getMetadataCondition(context)).build());

        ConversionProcessor<ActionMetadataEntity, ActionMetadata> processor = this.converterRegistry.getProcessor(
                ActionMetadataEntity.class, ActionMetadata.class);

        return interpreter.prepareDataQuery()
                          .getResultStream()
                          .findAny()
                          .map(processor::toItem)
                          .map(ConversionResult::result);
    }

    public Set<ActionContext> fetchActiveContexts(ActionContextMetadata context)
    {
        return this.fetchContexts(context, true);
    }

    public Set<ActionContext> fetchContexts(ActionContextMetadata context)
    {
        return this.fetchContexts(context, false);
    }

    private Set<ActionContext> fetchContexts(ActionContextMetadata context, boolean activeOnly)
    {
        DatasourceConditionDetail<Void> noItemId = DatasourceHelper.isNull(ActionContextEntity_.ITEM_ID);
        DatasourceConditionDetail<String> itemIdEquals = DatasourceHelper.isEqual(ActionContextEntity_.ITEM_ID,
                context.getItemId().orElse(null));

        DatasourceConditionInfo condition = DatasourceHelper.and(
                context.getItemId().isPresent() ? itemIdEquals : noItemId);

        DatasourceRelationInfo metadataRelation = DatasourceRelationInfo.builder()
                                                                        .property(
                                                                                ActionContextEntity_.METADATA.getName())
                                                                        .condition(this.getMetadataCondition(context))
                                                                        .build();

        DatasourceConditionDetail<String> notTerminated = DatasourceHelper.isNotPresent(ActionProcessEntity_.STATUS,
                BaseActionProcessStatus.TERMINATED.getName());

        DatasourceRelationInfo processRelation = DatasourceRelationInfo.builder()
                                                                       .property(
                                                                               ActionContextEntity_.PROCESSES.getName())
                                                                       .condition(DatasourceHelper.and(notTerminated))
                                                                       .build();

        DatasourceQuery.Generator query = DatasourceQuery.builder().condition(condition).relation(metadataRelation);
        if (activeOnly)
            query.relation(processRelation);

        DatasourceQueryInterpreter<ActionContextEntity> interpreter = DatasourceQueryInterpreter.of(
                ActionContextEntity.class, query.build());

        return this.converterRegistry.getProcessor(ActionContextEntity.class, ActionContext.class)
                                     .toSet(interpreter.prepareDataQuery().getResultList())
                                     .result();
    }

    private DatasourceConditionInfo getMetadataCondition(ActionContextMetadata context)
    {
        DatasourceConditionDetail<String> namespaceEquals = DatasourceHelper.isEqual(ActionMetadataEntity_.NAMESPACE,
                context.getNamespace());

        DatasourceConditionDetail<String> categoryEquals = DatasourceHelper.isEqual(ActionMetadataEntity_.CATEGORY,
                context.getCategory());

        DatasourceConditionDetail<String> nameEquals = DatasourceHelper.isEqual(ActionMetadataEntity_.NAME,
                context.getName());

        return DatasourceHelper.and(namespaceEquals, categoryEquals, nameEquals);
    }

    public Optional<ActionProcess> fetchCurrentState(ActionContextId contextId)
    {
        DatasourceConditionDetail<UUID> contextIdEquals = DatasourceHelper.isEqual(ActionProcessEntity_.CONTEXT_ID,
                contextId.getValue());

        DatasourceConditionInfo condition = DatasourceHelper.and(contextIdEquals);

        DatasourceQueryInterpreter<ActionProcessEntity> interpreter = DatasourceQueryInterpreter.of(
                ActionProcessEntity.class, DatasourceQuery.builder()
                                                          .condition(condition)
                                                          .order(DatasourceHelper.desc(ActionProcessEntity_.ID))
                                                          .build());

        ConversionProcessor<ActionProcessEntity, ActionProcess> processor = this.converterRegistry.getProcessor(
                ActionProcessEntity.class, ActionProcess.class);

        return interpreter.prepareDataQuery()
                          .getResultStream()
                          .findAny()
                          .map(processor::toItem)
                          .map(ConversionResult::result);
    }

    public Optional<ActionProcess> fetchCurrentState(String correlationId)
    {
        DatasourceConditionDetail<String> correlationIdEquals = DatasourceHelper.isEqual(
                ActionContextEntity_.CORRELATION_ID, correlationId);

        DatasourceConditionInfo condition = DatasourceHelper.and(correlationIdEquals);
        DatasourceRelationInfo contextRelation = DatasourceRelationInfo.builder()
                                                                       .property(ActionProcessEntity_.CONTEXT.getName())
                                                                       .condition(condition)
                                                                       .build();

        DatasourceQueryInterpreter<ActionProcessEntity> interpreter = DatasourceQueryInterpreter.of(
                ActionProcessEntity.class, DatasourceQuery.builder()
                                                          .relation(contextRelation)
                                                          .order(DatasourceHelper.desc(ActionProcessEntity_.ID))
                                                          .build());

        ConversionProcessor<ActionProcessEntity, ActionProcess> processor = this.converterRegistry.getProcessor(
                ActionProcessEntity.class, ActionProcess.class);

        return interpreter.prepareDataQuery()
                          .getResultStream()
                          .findAny()
                          .map(processor::toItem)
                          .map(ConversionResult::result);
    }

    public List<String> fetchAvailableActions(String correlationId)
    {
        ActionProcess process = this.fetchCurrentState(correlationId).orElseThrow();
        ActionMetadata metadata = this.metadataService.fetchByCorrelationId(correlationId).orElseThrow();

        return this.processorRegistry.getPossibleActions(metadata.getNamespace(), metadata.getCategory(),
                metadata.getName(), process.getStatus());
    }

    private Optional<String> getIdentityName( )
    {
        try
        {
            if (this.securityContext.getUserPrincipal() == null)
                return Optional.empty();

            return Optional.ofNullable(this.securityContext.getUserPrincipal().getName());
        }
        catch (ContextNotActiveException | IllegalStateException exception)
        {
            return Optional.empty();
        }
    }
}
