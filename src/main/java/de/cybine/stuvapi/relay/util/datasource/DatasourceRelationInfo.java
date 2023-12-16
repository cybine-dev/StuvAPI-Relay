package de.cybine.stuvapi.relay.util.datasource;

import de.cybine.stuvapi.relay.exception.datasource.*;
import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.query.*;

import java.util.*;

@Data
@SuppressWarnings("unused")
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceRelationInfo
{
    @NotNull
    private final String property;

    private final boolean fetch;

    private final DatasourceConditionInfo condition;

    @Singular("groupBy")
    private final List<String> groupingProperties;

    @Singular("order")
    private final List<DatasourceOrderInfo> order;

    @Singular
    private final List<DatasourceRelationInfo> relations;

    public Optional<DatasourceConditionInfo> getCondition( )
    {
        return Optional.ofNullable(this.condition);
    }

    public List<Path<Object>> getAllGroupings(Path<?> parent)
    {
        Path<?> path = parent.get(this.property);
        List<Path<Object>> groupings = new ArrayList<>();
        for (String groupBy : this.groupingProperties)
            groupings.add(DatasourceFieldPath.resolvePath(path, groupBy));

        for (DatasourceRelationInfo relation : this.relations)
            groupings.addAll(relation.getAllGroupings(path));

        return groupings;
    }

    public List<BiTuple<Order, Integer>> getAllOrderings(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        Path<?> path = parent.get(this.property);
        List<BiTuple<Order, Integer>> orderings = new ArrayList<>();
        for (DatasourceOrderInfo ordering : this.order)
            orderings.add(ordering.toOrder(criteriaBuilder, path));

        for (DatasourceRelationInfo relation : this.relations)
            orderings.addAll(relation.getAllOrderings(criteriaBuilder, path));

        return orderings;
    }

    public List<Predicate> getConditions(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        try
        {
            Path<?> path = parent.get(this.property);
            List<Predicate> conditions = new ArrayList<>();
            this.getCondition().flatMap(item -> item.toPredicate(criteriaBuilder, path)).ifPresent(conditions::add);

            for (DatasourceRelationInfo relation : this.relations)
                conditions.addAll(relation.getConditions(criteriaBuilder, path));

            return conditions;
        }
        catch (IllegalArgumentException exception)
        {
            if (exception.getCause() instanceof SemanticException semanticException)
            {
                throw new UnknownRelationException(semanticException.getMessage(), semanticException).addData("name",
                        this.property);
            }

            throw exception;
        }
    }

    public List<BiTuple<String, Object>> getParameters( )
    {
        List<BiTuple<String, Object>> parameters = new ArrayList<>();
        this.getCondition().map(DatasourceConditionInfo::getParameterValues).ifPresent(parameters::addAll);
        for (DatasourceRelationInfo relation : this.relations)
            parameters.addAll(relation.getParameters());

        return parameters;
    }

    public void addRelations(EntityGraph<?> parent)
    {
        Subgraph<Object> graph = parent.addSubgraph(this.property);
        for (DatasourceRelationInfo relation : this.relations)
        {
            if (!relation.isFetch())
                continue;

            relation.addRelations(graph);
        }
    }

    public void addRelations(Subgraph<?> parent)
    {
        Subgraph<Object> graph = parent.addSubgraph(this.property);
        for (DatasourceRelationInfo relation : this.relations)
        {
            if (!relation.isFetch())
                continue;

            relation.addRelations(graph);
        }
    }
}
