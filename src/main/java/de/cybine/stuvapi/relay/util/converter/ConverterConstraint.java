package de.cybine.stuvapi.relay.util.converter;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.util.*;

/**
 * <p>Constraints to evaluate while performing object conversions</p>
 */
@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConverterConstraint
{
    /**
     * Maximum allowed depth level for mapping
     */
    @JsonProperty("max_depth")
    private final Integer maxDepth;

    /**
     * Whether to remove null-values from collections
     */
    @JsonProperty("filter_null_values")
    private final Boolean filterNullValues;

    /**
     * Whether to allow empty collections for child-elements
     */
    @JsonProperty("allow_empty_collection")
    private final Boolean allowEmptyCollection;

    /**
     * Policy to evaluate when finding an already mapped object-key in previous mapping steps
     */
    @JsonProperty("duplicate_policy")
    private final DuplicatePolicy duplicatePolicy;

    /**
     * @return maximum allowed depth level for mapping
     */
    public Optional<Integer> getMaxDepth( )
    {
        return Optional.ofNullable(this.maxDepth);
    }

    /**
     * @return whether to remove null-values from collections
     */
    public Optional<Boolean> getFilterNullValues( )
    {
        return Optional.ofNullable(this.filterNullValues);
    }

    /**
     * @return whether to allow empty collections for child-elements
     */
    public Optional<Boolean> getAllowEmptyCollection( )
    {
        return Optional.ofNullable(this.allowEmptyCollection);
    }

    /**
     * @return policy to evaluate when finding an already mapped object-key in previous mapping steps
     */
    public Optional<DuplicatePolicy> getDuplicatePolicy( )
    {
        return Optional.ofNullable(this.duplicatePolicy);
    }

    public static ConverterConstraint create( )
    {
        return ConverterConstraint.builder()
                .maxDepth(5)
                .filterNullValues(true)
                .allowEmptyCollection(false)
                .duplicatePolicy(DuplicatePolicy.IGNORE_ALL)
                .build();
    }

    /**
     * <p>Policy to evaluate when finding an already mapped object-key in previous mapping steps</p>
     */
    public enum DuplicatePolicy
    {
        /**
         * <p>Perform mapping regardless of previous mapping steps</p>
         */
        PROCESS,

        /**
         * <p>Ignore single already mapped element and perform mapping on all other elements</p>
         */
        IGNORE_ITEM,

        /**
         * <p>Don't perform any mapping if any element has already been mapped</p>
         */
        IGNORE_ALL
    }
}