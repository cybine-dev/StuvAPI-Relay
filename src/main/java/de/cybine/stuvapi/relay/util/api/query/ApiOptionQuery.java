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
public class ApiOptionQuery
{
    @NotNull
    @JsonProperty("property")
    private final String property;

    @Valid
    @JsonProperty("pagination")
    private final ApiPaginationInfo pagination;

    @Valid
    @JsonProperty("condition")
    private final ApiConditionInfo condition;

    public Optional<ApiPaginationInfo> getPagination( )
    {
        return Optional.ofNullable(this.pagination);
    }

    public Optional<ApiConditionInfo> getCondition( )
    {
        return Optional.ofNullable(this.condition);
    }
}
