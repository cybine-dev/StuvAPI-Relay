package de.cybine.stuvapi.relay.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.jackson.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiCountQuery
{
    @Valid
    @JsonProperty("condition")
    private final ApiConditionInfo condition;

    @Singular("groupBy")
    @JsonProperty("group_by")
    private final List<String> groupingProperties;

    @Valid
    @Singular
    @JsonProperty("relations")
    private final List<ApiCountRelationInfo> relations;
}
