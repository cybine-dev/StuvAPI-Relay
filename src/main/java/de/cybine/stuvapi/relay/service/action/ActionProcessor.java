package de.cybine.stuvapi.relay.service.action;

public interface ActionProcessor<T>
{
    ActionProcessorMetadata getMetadata( );

    boolean shouldExecute(ActionStateTransition transition);

    ActionProcessorResult<T> process(ActionStateTransition transition);
}
