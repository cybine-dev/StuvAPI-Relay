package de.cybine.stuvapi.relay.data.action.context;

import com.fasterxml.jackson.annotation.*;
import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.service.action.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionContextMetadata
{
    @JsonProperty("namespace")
    private final String namespace;

    @JsonProperty("category")
    private final String category;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("item_id")
    private final String itemId;

    public Optional<String> getItemId( )
    {
        return Optional.ofNullable(this.itemId);
    }

    public ActionProcessorMetadata toProcessorMetadata(String fromState, String toState)
    {
        return ActionProcessorMetadata.builder()
                                      .namespace(this.namespace)
                                      .category(this.category)
                                      .name(this.name)
                                      .fromStatus(fromState)
                                      .toStatus(toState)
                                      .build();
    }

    public static ActionContextMetadata of(ActionMetadata metadata)
    {
        return ActionContextMetadata.of(metadata, null);
    }

    public static ActionContextMetadata of(ActionMetadata metadata, String itemId)
    {
        return ActionContextMetadata.builder()
                                    .namespace(metadata.getNamespace())
                                    .category(metadata.getCategory())
                                    .name(metadata.getName())
                                    .itemId(itemId)
                                    .build();
    }
}
