package de.cybine.stuvapi.relay.util.datasource;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.stuvapi.relay.util.*;
import de.cybine.stuvapi.relay.util.api.query.ApiConditionDetail.*;
import io.quarkus.arc.*;
import io.smallrye.common.constraint.*;
import jakarta.persistence.criteria.*;
import lombok.*;

import java.util.*;

@Data
@SuppressWarnings("unused")
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasourceConditionDetail<T>
{
    @Builder.Default
    private final UUID id = UUID.randomUUID();

    @With
    @NotNull
    private final String property;

    private final Type type;

    private final T value;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ParameterExpression<? extends T> parameter;

    public Optional<T> getValue( )
    {
        return Optional.ofNullable(this.value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Predicate toPredicate(CriteriaBuilder criteriaBuilder, Path<?> parent)
    {
        Path<T> path = DatasourceFieldPath.resolvePath(parent, this.property);
        ParameterExpression<? extends T> param = this.toParameter(criteriaBuilder, path);

        return switch (this.type)
        {
            case IS_NULL -> criteriaBuilder.isNull(path);
            case IS_NOT_NULL -> criteriaBuilder.isNotNull(path);

            case IS_EQUAL -> criteriaBuilder.equal(path, param);
            case IS_NOT_EQUAL -> criteriaBuilder.notEqual(path, param);

            case IS_LIKE -> criteriaBuilder.like((Path<String>) path, (Expression<String>) param);
            case IS_NOT_LIKE -> criteriaBuilder.notLike((Path<String>) path, (Expression<String>) param);

            case IS_IN -> path.in(param);
            case IS_NOT_IN -> criteriaBuilder.not(path.in(param));

            case IS_PRESENT -> param.in(path);
            case IS_NOT_PRESENT -> criteriaBuilder.not(param.in(path));

            case IS_GREATER -> criteriaBuilder.greaterThan((Path) path, (Expression) param);
            case IS_GREATER_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo((Path) path, (Expression) param);
            case IS_LESS -> criteriaBuilder.lessThan((Path) path, (Expression) param);
            case IS_LESS_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo((Path) path, (Expression) param);
        };
    }

    public Optional<BiTuple<String, Object>> toParameterValue( )
    {
        if (!this.type.requiresData())
            return Optional.empty();

        return Optional.of(new BiTuple<>(this.getName(), this.value));
    }

    public String getName( )
    {
        return this.property + "-" + this.getId().toString();
    }

    @SuppressWarnings("unchecked")
    private ParameterExpression<? extends T> toParameter(CriteriaBuilder criteriaBuilder, Path<T> property)
    {
        if (this.parameter == null)
        {
            TypeFactory typeFactory = Arc.container().select(ObjectMapper.class).get().getTypeFactory();

            JavaType itemType = typeFactory.constructType(property.getJavaType());
            if (this.type.requiresIterable())
                itemType = typeFactory.constructCollectionType(List.class, itemType);

            this.parameter = (ParameterExpression<? extends T>) criteriaBuilder.parameter(itemType.getRawClass(),
                    this.getName());
        }

        return this.parameter;
    }
}
