package de.cybine.stuvapi.relay.service.action;

import de.cybine.stuvapi.relay.data.action.process.*;
import lombok.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionStateTransition
{
    private final ActionService service;

    @Getter(AccessLevel.NONE)
    private final ActionProcessorMetadata metadata;

    private final ActionProcess         previousState;
    private final ActionProcessMetadata nextState;

    public String getNamespace( )
    {
        return this.metadata.getNamespace();
    }

    public String getCategory( )
    {
        return this.metadata.getCategory();
    }

    public String getName( )
    {
        return this.metadata.getName();
    }
}
