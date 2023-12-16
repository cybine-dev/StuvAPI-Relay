package de.cybine.stuvapi.relay.util.datasource;

import de.cybine.stuvapi.relay.util.*;
import de.cybine.stuvapi.relay.util.api.query.ApiConditionInfo.*;
import jakarta.persistence.criteria.*;
import lombok.*;

import java.util.*;

@Data
@SuppressWarnings("unused")
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceConditionInfo
{
    @Builder.Default
    private final EvaluationMethod type = EvaluationMethod.AND;

    @Builder.Default
    private final boolean isInverted = false;

    @Singular
    private final List<DatasourceConditionDetail<?>> details;

    @Singular
    private final List<DatasourceConditionInfo> subConditions;



    public Optional<Predicate> toPredicate(CriteriaBuilder criteriaBuilder, Path<?> path)
    {
        Predicate predicate = null;
        for (DatasourceConditionDetail<?> detail : this.details)
        {
            Predicate detailPredicate = detail.toPredicate(criteriaBuilder, path);
            if (predicate == null)
            {
                predicate = detailPredicate;
                continue;
            }

            predicate = switch (this.type)
            {
                case AND -> criteriaBuilder.and(predicate, detailPredicate);
                case OR -> criteriaBuilder.or(predicate, detailPredicate);
            };
        }

        for (DatasourceConditionInfo subCondition : this.subConditions)
        {
            Predicate subConditionPredicate = subCondition.toPredicate(criteriaBuilder, path).orElse(null);
            if (subConditionPredicate == null)
                continue;

            if (predicate == null)
            {
                predicate = subConditionPredicate;
                continue;
            }

            predicate = switch (this.type)
            {
                case AND -> criteriaBuilder.and(predicate, subConditionPredicate);
                case OR -> criteriaBuilder.or(predicate, subConditionPredicate);
            };
        }

        return Optional.ofNullable(this.isInverted ? criteriaBuilder.not(predicate) : predicate);
    }

    public List<BiTuple<String, Object>> getParameterValues()
    {
        List<BiTuple<String, Object>> values = new ArrayList<>();
        for(DatasourceConditionDetail<?> detail : this.details)
            detail.toParameterValue().ifPresent(values::add);

        for(DatasourceConditionInfo subCondition : this.subConditions)
            values.addAll(subCondition.getParameterValues());

        return values;
    }
}
