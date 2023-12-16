package de.cybine.stuvapi.relay.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiRelationInfo
{
    @NotNull
    @JsonProperty("property")
    private final String property;

    @Builder.Default
    @JsonProperty("fetch")
    private final boolean fetch = false;

    @Valid
    @JsonProperty("condition")
    private final ApiConditionInfo condition;

    @Valid
    @Singular("order")
    @JsonProperty("order")
    private final List<ApiOrderInfo> order;

    @Valid
    @Singular
    @JsonProperty("relations")
    private final List<ApiRelationInfo> relations;

    public Optional<ApiConditionInfo> getCondition( )
    {
        return Optional.ofNullable(this.condition);
    }
}
