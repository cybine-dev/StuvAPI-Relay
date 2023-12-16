package de.cybine.stuvapi.relay.service.action;

import lombok.*;

import java.util.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionProcessorMetadata
{
    private final String namespace;
    private final String category;
    private final String name;

    private final String fromStatus;
    private final String toStatus;

    public Optional<String> getFromStatus( )
    {
        return Optional.ofNullable(this.fromStatus);
    }

    public String asString( )
    {
        return String.format("ns(%s) cat(%s) name(%s) from(%s) to(%s)", this.getNamespace(), this.getCategory(),
                this.getName(), this.getFromStatus().orElse(null), this.getToStatus());
    }
}
