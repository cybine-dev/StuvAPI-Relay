package de.cybine.stuvapi.relay.data.action.context;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.data.util.*;
import de.cybine.stuvapi.relay.util.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionContext implements Serializable, WithId<ActionContextId>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @JsonDeserialize(using = ActionContextId.Deserializer.class)
    private final ActionContextId id;

    @JsonProperty("metadata_id")
    @JsonView(Views.Simple.class)
    @JsonDeserialize(using = ActionMetadataId.Deserializer.class)
    private final ActionMetadataId metadataId;

    @JsonProperty("metadata")
    @JsonView(Views.Extended.class)
    private final ActionMetadata metadata;

    @Builder.Default
    @JsonProperty("correlation_id")
    private final String correlationId = UUIDv7.generate().toString();

    @JsonProperty("item_id")
    private final String itemId;

    @JsonProperty("processes")
    @JsonView(Views.Extended.class)
    private final Set<ActionProcess> processes;

    public Optional<ActionMetadata> getMetadata( )
    {
        return Optional.ofNullable(this.metadata);
    }

    public Optional<String> getItemId( )
    {
        return Optional.ofNullable(this.itemId);
    }

    public Optional<Set<ActionProcess>> getProcesses( )
    {
        return Optional.ofNullable(this.processes);
    }

    @JsonProperty("process_ids")
    @JsonView(Views.Simple.class)
    private Optional<Set<ActionProcessId>> getProcessIds( )
    {
        return this.getProcesses().map(items -> items.stream().map(WithId::getId).collect(Collectors.toSet()));
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;

        if (this.getClass() != other.getClass())
            return false;

        WithId<?> that = ((WithId<?>) other);
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}
