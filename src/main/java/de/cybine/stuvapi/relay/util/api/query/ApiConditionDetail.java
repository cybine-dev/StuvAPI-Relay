package de.cybine.stuvapi.relay.util.api.query;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.*;
import lombok.extern.jackson.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiConditionDetail
{
    @With
    @NotNull
    @JsonProperty("property")
    private final String property;

    @NotNull
    @JsonProperty("type")
    private final Type type;

    @JsonProperty("value")
    private final Object value;

    public Optional<Object> getValue( )
    {
        return Optional.ofNullable(this.value);
    }

    @Getter
    @Accessors(fluent = true)
    @Schema(name = "ApiConditionType")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type
    {
        IS_NULL(false, false),
        IS_NOT_NULL(false, false),
        IS_EQUAL(true, false),
        IS_NOT_EQUAL(true, false),
        IS_LIKE(true, false),
        IS_NOT_LIKE(true, false),
        IS_IN(true, true),
        IS_NOT_IN(true, true),
        IS_PRESENT(true, false),
        IS_NOT_PRESENT(true, false),
        IS_GREATER(true, false),
        IS_GREATER_OR_EQUAL(true, false),
        IS_LESS(true, false),
        IS_LESS_OR_EQUAL(true, false);

        private final boolean requiresData;
        private final boolean requiresIterable;
    }
}
