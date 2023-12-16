package de.cybine.stuvapi.relay.util.datasource;

import de.cybine.stuvapi.relay.util.api.query.ApiConditionDetail.*;
import de.cybine.stuvapi.relay.util.api.query.ApiConditionInfo.*;
import lombok.experimental.*;

import java.util.*;

@UtilityClass
@SuppressWarnings("unused")
public class DatasourceHelper
{
    public static DatasourceConditionInfo and(DatasourceConditionDetail<?>... conditions)
    {
        return DatasourceConditionInfo.builder()
                                      .type(EvaluationMethod.AND)
                                      .details(Arrays.stream(conditions).toList())
                                      .build();
    }

    public static DatasourceConditionInfo andNot(DatasourceConditionDetail<?>... conditions)
    {
        return DatasourceConditionInfo.builder()
                                      .type(EvaluationMethod.AND)
                                      .isInverted(true)
                                      .details(Arrays.stream(conditions).toList())
                                      .build();
    }

    public static DatasourceConditionInfo or(DatasourceConditionDetail<?>... conditions)
    {
        return DatasourceConditionInfo.builder()
                                      .type(EvaluationMethod.OR)
                                      .details(Arrays.stream(conditions).toList())
                                      .build();
    }

    public static DatasourceConditionInfo orNot(DatasourceConditionDetail<?>... conditions)
    {
        return DatasourceConditionInfo.builder()
                                      .type(EvaluationMethod.OR)
                                      .isInverted(true)
                                      .details(Arrays.stream(conditions).toList())
                                      .build();
    }

    public static DatasourceConditionDetail<Void> isNull(DatasourceField property)
    {
        return DatasourceConditionDetail.<Void>builder().property(property.getName()).type(Type.IS_NULL).build();
    }

    public static DatasourceConditionDetail<Void> isNotNull(DatasourceField property)
    {
        return DatasourceConditionDetail.<Void>builder().property(property.getName()).type(Type.IS_NOT_NULL).build();
    }

    public static <T> DatasourceConditionDetail<T> isEqual(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_EQUAL)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isNotEqual(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_NOT_EQUAL)
                                        .value(data)
                                        .build();
    }

    public static DatasourceConditionDetail<String> isLike(DatasourceField property, String data)
    {
        return DatasourceConditionDetail.<String>builder()
                                        .property(property.getName())
                                        .type(Type.IS_LIKE)
                                        .value(data)
                                        .build();
    }

    public static DatasourceConditionDetail<String> isNotEqual(DatasourceField property, String data)
    {
        return DatasourceConditionDetail.<String>builder()
                                        .property(property.getName())
                                        .type(Type.IS_NOT_LIKE)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<Collection<T>> isIn(DatasourceField property, Collection<T> data)
    {
        return DatasourceConditionDetail.<Collection<T>>builder()
                                        .property(property.getName())
                                        .type(Type.IS_IN)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<Collection<T>> isNotIn(DatasourceField property, Collection<T> data)
    {
        return DatasourceConditionDetail.<Collection<T>>builder()
                                        .property(property.getName())
                                        .type(Type.IS_NOT_IN)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isPresent(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_PRESENT)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isNotPresent(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_NOT_PRESENT)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isGreater(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_GREATER)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isGreaterOrEqual(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_GREATER_OR_EQUAL)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isLess(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_LESS)
                                        .value(data)
                                        .build();
    }

    public static <T> DatasourceConditionDetail<T> isLessOrEqual(DatasourceField property, T data)
    {
        return DatasourceConditionDetail.<T>builder()
                                        .property(property.getName())
                                        .type(Type.IS_LESS_OR_EQUAL)
                                        .value(data)
                                        .build();
    }

    public static DatasourceRelationInfo fetch(DatasourceField property)
    {
        return DatasourceRelationInfo.builder().property(property.getName()).fetch(true).build();
    }

    public static DatasourceOrderInfo asc(DatasourceField property)
    {
        return DatasourceOrderInfo.builder().property(property.getName()).isAscending(true).build();
    }

    public static DatasourceOrderInfo desc(DatasourceField property)
    {
        return DatasourceOrderInfo.builder().property(property.getName()).isAscending(false).build();
    }
}
