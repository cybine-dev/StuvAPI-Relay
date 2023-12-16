package de.cybine.stuvapi.relay.data.action.metadata;

import de.cybine.stuvapi.relay.util.datasource.*;
import lombok.*;

import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ActionType implements WithDatasourceKey<String>
{
    UPDATE("update", true), ACTION("action", false), LOG("log", false);

    private final String name;

    private final boolean isExclusive;

    @Override
    public String getDatasourceKey( )
    {
        return this.getName();
    }

    public static Optional<ActionType> findByName(String name)
    {
        if (name == null)
            return Optional.empty();

        return Arrays.stream(ActionType.values()).filter(item -> item.getName().equals(name)).findAny();
    }
}
