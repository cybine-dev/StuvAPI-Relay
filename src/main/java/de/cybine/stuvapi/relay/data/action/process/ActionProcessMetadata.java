package de.cybine.stuvapi.relay.data.action.process;

import com.fasterxml.jackson.annotation.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.service.action.data.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.time.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionProcessMetadata
{
    @JsonProperty("context_id")
    private final ActionContextId contextId;

    @JsonProperty("status")
    private final String status;

    @JsonProperty("priority")
    private final Integer priority;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("created_at")
    private final ZonedDateTime createdAt;

    @JsonProperty("due_at")
    private final ZonedDateTime dueAt;

    @JsonProperty("data")
    private final ActionData<?> data;

    public Optional<Integer> getPriority( )
    {
        return Optional.ofNullable(this.priority);
    }

    public Optional<String> getDescription( )
    {
        return Optional.ofNullable(this.description);
    }

    public Optional<ZonedDateTime> getDueAt( )
    {
        return Optional.ofNullable(this.dueAt);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<ActionData<T>> getData( )
    {
        return Optional.ofNullable((ActionData<T>) this.data);
    }
}
