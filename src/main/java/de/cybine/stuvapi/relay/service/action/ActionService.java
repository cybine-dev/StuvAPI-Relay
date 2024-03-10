package de.cybine.stuvapi.relay.service.action;

import de.cybine.quarkus.exception.action.*;
import de.cybine.quarkus.util.*;
import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.action.stateful.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import io.quarkus.runtime.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hibernate.*;

import java.time.*;
import java.util.*;

@Slf4j
@Startup
@Singleton
@RequiredArgsConstructor
public class ActionService implements StatefulActionService
{
    public static final String TERMINATED_STATE = "terminated";

    private final List<Workflow>        workflows  = new ArrayList<>();
    private final List<ActionProcessor> processors = new ArrayList<>();

    private final ConverterRegistry converterRegistry;
    private final ContextService    contextService;

    private final SessionFactory  sessionFactory;
    private final SecurityContext securityContext;

    @Override
    public void registerWorkflow(Workflow workflow)
    {
        if (this.findWorkflow(workflow.getNamespace(), workflow.getCategory(), workflow.getName()).isPresent())
            throw new DuplicateProcessorDefinitionException(
                    String.format("Workflow %s is already registered.", workflow.toShortForm()));

        this.workflows.add(workflow);
    }

    @Override
    public Optional<Workflow> findWorkflow(String namespace, String context, String name)
    {
        for (Workflow workflow : this.workflows)
        {
            boolean hasSameNamespace = workflow.getNamespace().equals(namespace);
            boolean hasSameContext = workflow.getCategory().equals(context);
            boolean hasSameName = workflow.getName().equals(name);
            if (!hasSameNamespace || !hasSameContext || !hasSameName)
                continue;

            return Optional.of(workflow);
        }

        return Optional.empty();
    }

    @Override
    public List<ActionProcessorMetadata> availableActions(String correlationId)
    {
        ActionProcess process = this.fetchCurrentState(correlationId).orElseThrow();
        ActionContext context = this.contextService.fetchByCorrelationId(correlationId).orElseThrow();
        Workflow workflow = this.findWorkflow(context.getNamespace(), context.getCategory(), context.getName())
                                .orElse(null);

        if (workflow == null)
            return Collections.emptyList();

        List<ActionProcessorMetadata> availableActions = new ArrayList<>();
        for (ActionProcessorMetadata step : workflow.getWorkflowSteps())
        {
            boolean hasSameAction = step.getAction().equals(process.getStatus());
            boolean hasApplicableStatus = step.getFrom().map(item -> item.equals(process.getStatus())).orElse(true);
            if (!hasSameAction || !hasApplicableStatus)
                continue;

            availableActions.add(step);
        }

        return availableActions;
    }

    @Override
    public void registerProcessor(ActionProcessor processor)
    {
        if (this.processors.stream().anyMatch(item -> item.getMetadata().equals(processor.getMetadata())))
            throw new DuplicateProcessorDefinitionException("Processor already present");

        this.processors.add(processor);
    }

    private Optional<ActionProcessor> findProcessor(ActionMetadata metadata, String currentState)
    {
        return this.findProcessor(ActionProcessorMetadata.builder()
                                                         .namespace(metadata.getNamespace())
                                                         .category(metadata.getCategory())
                                                         .name(metadata.getName())
                                                         .action(metadata.getAction())
                                                         .from(currentState)
                                                         .build());
    }

    @Override
    public Optional<ActionProcessor> findProcessor(ActionProcessorMetadata metadata)
    {
        String shortForm = metadata.toShortForm() + metadata.getFrom().map(from -> ":" + from).orElse("");
        return this.processors.stream().filter(item -> item.getMetadata().isApplicable(shortForm)).findAny();
    }

    @Override
    public List<ActionResult<?>> bulkPerform(List<Action> actions)
    {
        if (actions == null || actions.isEmpty())
            return Collections.emptyList();

        ActionMetadata metadata = actions.get(0).getMetadata();
        ActionContext context = metadata.getCorrelationId()
                                        .flatMap(this.contextService::fetchByCorrelationId)
                                        .orElse(null);

        if (context == null)
            throw new UnknownActionStateException(
                    "Could not find action-state for correlation-id " + metadata.getCorrelationId().orElse(null));

        List<ActionResult<?>> results = new ArrayList<>();
        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            log.debug("Started processing bulk-action... [{}]", metadata.toShortForm());

            ActionProcess previousAction = metadata.getCorrelationId().flatMap(this::fetchCurrentState).orElseThrow();
            ActionResult<?> result = this.processToResult(context, previousAction);

            long counter = 0;
            for (Action action : actions)
            {
                try
                {
                    ActionHelper helper = this.createHelper(action.getMetadata(), result);
                    result = this.$process(action, helper, context.getId()).second();

                    results.add(result);

                    if (++counter % 250 == 0)
                    {
                        log.debug("Processed {} actions [{}]", counter, metadata.toShortForm());

                        session.flush();
                        session.clear();
                    }
                }
                catch (ActionPreconditionException ignored)
                {
                    // NOOP
                }
            }

            log.debug("Committing bulk-action... [{}]", metadata.toShortForm());
            transaction.commit();
        }

        log.debug("Finished processing bulk-action. [{}]", metadata.toShortForm());
        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ActionResult<T> perform(Action action)
    {
        ActionMetadata metadata = action.getMetadata();
        ActionProcess previousAction = metadata.getCorrelationId().flatMap(this::fetchCurrentState).orElseThrow();

        ActionProcessor processor = this.findProcessor(metadata, previousAction.getStatus()).orElse(null);
        if (processor == null)
            throw new UnknownActionException(String.format("Action of type %s is unknown", metadata.toShortForm()));

        ActionContext context = action.getCorrelationId()
                                      .flatMap(this.contextService::fetchByCorrelationId)
                                      .orElse(null);
        if (context == null)
            throw new UnknownActionStateException(
                    "Could not find action-state for correlation-id " + metadata.getCorrelationId().orElseThrow());

        ActionHelper helper = this.createHelper(metadata, this.processToResult(context, previousAction));
        if (!processor.shouldExecute(action, helper))
            throw new ActionPreconditionException("Action did not fulfill precondition.");

        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            ActionResult<T> result = (ActionResult<T>) this.$process(action, helper, context.getId()).second();

            transaction.commit();

            return result;
        }
    }

    @Override
    public String beginWorkflow(String namespace, String context, String name)
    {
        return this.beginWorkflow(namespace, context, name, null);
    }

    @Override
    public String beginWorkflow(String namespace, String category, String name, String itemId)
    {
        Workflow workflow = this.findWorkflow(namespace, category, name).orElse(null);
        if (workflow == null)
            throw new UnknownActionException(
                    String.format("No workflow for action %s:%s:%s found", namespace, category, name));

        if (itemId != null && !this.fetchActiveContexts(namespace, category, name, itemId).isEmpty())
            throw new AmbiguousActionException(
                    String.format("Workflow %s already active for item-id %s", workflow.toShortForm(), itemId));

        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            ActionContext actionContext = ActionContext.builder()
                                                       .id(ActionContextId.create())
                                                       .namespace(namespace)
                                                       .category(category)
                                                       .name(name)
                                                       .itemId(itemId)
                                                       .build();

            ActionContextEntity entity = this.converterRegistry.getProcessor(ActionContext.class,
                    ActionContextEntity.class).toItem(actionContext).result();

            session.persist(entity);
            session.flush();

            ActionContextId contextId = ActionContextId.of(entity.getId());
            ActionProcess process = ActionProcess.builder()
                                                 .id(ActionProcessId.create())
                                                 .context(ActionContext.builder().id(contextId).build())
                                                 .status(workflow.getInitialState())
                                                 .creatorId(this.getIdentityName().orElse(null))
                                                 .createdAt(ZonedDateTime.now())
                                                 .build();

            session.persist(this.converterRegistry.getProcessor(ActionProcess.class, ActionProcessEntity.class)
                                                  .toItem(process)
                                                  .result());

            transaction.commit();

            return entity.getCorrelationId();
        }
    }

    public void updateItemId(String correlationId, String itemId)
    {
        Session session = this.sessionFactory.getCurrentSession();
        session.createNativeMutationQuery(
                       String.format("UPDATE %s SET %s = :itemId WHERE %s = :correlationId", ActionContextEntity_.TABLE,
                               ActionContextEntity_.ITEM_ID_COLUMN, ActionContextEntity_.CORRELATION_ID_COLUMN))
               .setParameter("itemId", itemId)
               .setParameter("correlationId", correlationId)
               .executeUpdate();
    }

    private <T> BiTuple<ActionProcess, ActionResult<T>> $process(Action action, ActionHelper helper,
            ActionContextId contextId)
    {
        String previousState = helper.getPrevious()
                                     .map(ActionResult::getMetadata)
                                     .map(ActionMetadata::getAction)
                                     .orElseThrow();

        ActionMetadata metadata = action.getMetadata();
        ActionProcessor processor = this.findProcessor(metadata, previousState).orElse(null);
        if (processor == null)
            throw new UnknownActionException("Unknown action " + action.toShortForm());

        if (!processor.shouldExecute(action, helper))
            throw new ActionPreconditionException(null);

        ActionProcess process = ActionProcess.builder()
                                             .id(ActionProcessId.create())
                                             .eventId(metadata.getEventId())
                                             .context(ActionContext.builder().id(contextId).build())
                                             .status(action.getAction())
                                             .priority(action.getPriority().orElse(100))
                                             .description(action.getDescription().orElse(null))
                                             .creatorId(this.getIdentityName().or(metadata::getSource).orElse(null))
                                             .createdAt(action.getCreatedAt().orElse(ZonedDateTime.now()))
                                             .dueAt(action.getDueAt().orElse(null))
                                             .data(action.getData().orElse(null))
                                             .build();

        Session session = this.sessionFactory.getCurrentSession();
        session.persist(this.converterRegistry.getProcessor(ActionProcess.class, ActionProcessEntity.class)
                                              .toItem(process)
                                              .result());

        return new BiTuple<>(process, processor.apply(action, helper));
    }

    public Set<ActionContext> fetchActiveContexts(String namespace, String category, String name, String itemId)
    {
        DatasourceConditionDetail<String> namespaceEquals = DatasourceHelper.isEqual(ActionContextEntity_.NAMESPACE,
                namespace);

        DatasourceConditionDetail<String> categoryEquals = DatasourceHelper.isEqual(ActionContextEntity_.CATEGORY,
                category);

        DatasourceConditionDetail<String> nameEquals = DatasourceHelper.isEqual(ActionContextEntity_.NAME, name);

        DatasourceConditionDetail<Void> noItemId = DatasourceHelper.isNull(ActionContextEntity_.ITEM_ID);
        DatasourceConditionDetail<String> itemIdEquals = DatasourceHelper.isEqual(ActionContextEntity_.ITEM_ID, itemId);

        DatasourceConditionInfo condition = DatasourceHelper.and(namespaceEquals, categoryEquals, nameEquals,
                itemId != null ? itemIdEquals : noItemId);

        DatasourceConditionDetail<String> notTerminated = DatasourceHelper.isNotPresent(ActionProcessEntity_.STATUS,
                TERMINATED_STATE);

        DatasourceRelationInfo processRelation = DatasourceRelationInfo.builder()
                                                                       .property(
                                                                               ActionContextEntity_.PROCESSES.getName())
                                                                       .condition(DatasourceHelper.and(notTerminated))
                                                                       .build();

        DatasourceQueryInterpreter<ActionContextEntity> interpreter = DatasourceQueryInterpreter.of(
                ActionContextEntity.class,
                DatasourceQuery.builder().condition(condition).relation(processRelation).build());

        return this.converterRegistry.getProcessor(ActionContextEntity.class, ActionContext.class)
                                     .toSet(interpreter.prepareDataQuery().getResultList())
                                     .result();
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

    private ActionResult<?> processToResult(ActionContext context, ActionProcess process)
    {
        ActionMetadata metadata = ActionMetadata.builder()
                                                .namespace(context.getNamespace())
                                                .category(context.getCategory())
                                                .name(context.getName())
                                                .action(process.getStatus())
                                                .priority(process.getPriority().orElse(null))
                                                .eventId(process.getEventId())
                                                .itemId(context.getItemId().orElse(null))
                                                .correlationId(context.getCorrelationId())
                                                .source(process.getCreatorId().orElse(null))
                                                .description(process.getDescription().orElse(null))
                                                .createdAt(process.getCreatedAt())
                                                .dueAt(process.getDueAt().orElse(null))
                                                .build();

        return ActionResult.of(metadata, process.getData().orElse(null));
    }

    private ActionHelper createHelper(ActionMetadata metadata, ActionResult<?> previousState)
    {
        return new ActionHelper(this, metadata, previousState, this::updateItemId);
    }
}
