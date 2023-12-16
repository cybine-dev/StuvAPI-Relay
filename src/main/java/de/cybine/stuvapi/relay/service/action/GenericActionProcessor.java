package de.cybine.stuvapi.relay.service.action;

import lombok.*;

import java.util.function.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GenericActionProcessor<T> implements ActionProcessor<T>
{
    private static final DefaultAction       DEFAULT_ACTION       = new DefaultAction();
    private static final DefaultPrecondition DEFAULT_PRECONDITION = new DefaultPrecondition();

    private final ActionProcessorMetadata metadata;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Predicate<ActionStateTransition> precondition;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Function<ActionStateTransition, ActionProcessorResult<T>> action;

    @Override
    public boolean shouldExecute(ActionStateTransition transition)
    {
        return this.precondition.test(transition);
    }

    @Override
    public ActionProcessorResult<T> process(ActionStateTransition transition)
    {
        return this.action.apply(transition);
    }

    public static GenericActionProcessor<Void> of(ActionProcessorMetadata metadata)
    {
        return new GenericActionProcessor<>(metadata, DEFAULT_PRECONDITION, DEFAULT_ACTION);
    }

    public static <T> GenericActionProcessor<T> of(ActionProcessorMetadata metadata,
            Function<ActionStateTransition, ActionProcessorResult<T>> action)
    {
        return new GenericActionProcessor<>(metadata, DEFAULT_PRECONDITION, action);
    }

    public static <T> GenericActionProcessor<T> of(ActionProcessorMetadata metadata,
            Function<ActionStateTransition, ActionProcessorResult<T>> action,
            Predicate<ActionStateTransition> precondition)
    {
        return new GenericActionProcessor<>(metadata, precondition, action);
    }

    private static class DefaultPrecondition implements Predicate<ActionStateTransition>
    {
        @Override
        public boolean test(ActionStateTransition transition)
        {
            return true;
        }
    }

    private static class DefaultAction implements Function<ActionStateTransition, ActionProcessorResult<Void>>
    {
        @Override
        public ActionProcessorResult<Void> apply(ActionStateTransition transition)
        {
            return ActionProcessorResult.empty();
        }
    }
}
