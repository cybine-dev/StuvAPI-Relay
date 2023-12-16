package de.cybine.stuvapi.relay.util.datasource;

import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceOrderInfo
{
    @NotNull
    private final String property;

    @Builder.Default
    private final int priority = 100;

    @Builder.Default
    private final boolean isAscending = true;

    public BiTuple<Order, Integer> toOrder(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        Path<?> path = DatasourceFieldPath.resolvePath(parent, this.property);
        return new BiTuple<>(this.isAscending ? criteriaBuilder.asc(path) : criteriaBuilder.desc(path), this.priority);
    }
}
