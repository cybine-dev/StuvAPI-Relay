package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ApiCapabilityScope
{
    ALL("all"), OWN("own"), OTHERS("others");

    @JsonValue
    private final String scope;

    public static Optional<ApiCapabilityScope> findByScope(String scope)
    {
        if (scope == null)
            return Optional.empty();

        return Arrays.stream(ApiCapabilityScope.values()).filter(item -> item.getScope().equals(scope)).findAny();
    }
}
