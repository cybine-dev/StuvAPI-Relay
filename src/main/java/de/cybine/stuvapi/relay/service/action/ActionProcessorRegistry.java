package de.cybine.stuvapi.relay.service.action;

import de.cybine.stuvapi.relay.exception.action.*;
import jakarta.enterprise.context.*;
import lombok.*;
import lombok.extern.slf4j.*;

import java.util.*;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ActionProcessorRegistry
{
    private final Map<ActionProcessorMetadata, ActionProcessor<?>> processors = new HashMap<>();

    public void registerProcessors(List<ActionProcessor<?>> processors)
    {
        for (ActionProcessor<?> processor : processors)
            this.registerProcessor(processor);
    }

    public void registerProcessor(ActionProcessor<?> processor)
    {
        ActionProcessorMetadata metadata = processor.getMetadata();
        if (this.processors.containsKey(metadata))
            throw new DuplicateProcessorDefinitionException(metadata.asString());

        log.debug("Registering action-processor: {}", metadata.asString());

        this.processors.put(processor.getMetadata(), processor);
    }

    public void removeProcessor(ActionProcessorMetadata metadata)
    {
        this.processors.remove(metadata);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ActionProcessor<T>> findProcessor(ActionProcessorMetadata metadata)
    {
        log.trace("Searching action-processor: {}", metadata.asString());

        ActionProcessor<?> actionProcessor = this.processors.get(metadata);
        if (actionProcessor != null)
            return Optional.of((ActionProcessor<T>) actionProcessor);

        return Optional.ofNullable((ActionProcessor<T>) this.processors.get(this.getWildcardMetadata(metadata)));
    }

    public List<String> getPossibleActions(String namespace, String category, String name, String status)
    {
        return this.processors.keySet()
                              .stream()
                              .filter(metadata -> metadata.getNamespace().equals(namespace))
                              .filter(metadata -> metadata.getCategory().equals(category))
                              .filter(metadata -> metadata.getName().equals(name))
                              .filter(metadata -> metadata.getFromStatus()
                                                          .map(item -> item.equals(status))
                                                          .orElse(true))
                              .map(ActionProcessorMetadata::getToStatus)
                              .toList();
    }

    private ActionProcessorMetadata getWildcardMetadata(ActionProcessorMetadata metadata)
    {
        return ActionProcessorMetadata.builder()
                                      .namespace(metadata.getNamespace())
                                      .category(metadata.getCategory())
                                      .name(metadata.getName())
                                      .toStatus(metadata.getToStatus())
                                      .build();
    }
}
