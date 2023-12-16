package de.cybine.stuvapi.relay.util.datasource;

import de.cybine.stuvapi.relay.exception.datasource.*;
import de.cybine.stuvapi.relay.util.*;
import io.quarkus.arc.*;
import jakarta.persistence.Parameter;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hibernate.jpa.*;

import java.lang.reflect.*;
import java.util.*;

@Slf4j
@SuppressWarnings("unused")
@AllArgsConstructor(staticName = "of")
public class DatasourceQueryInterpreter<T>
{
    private final EntityManager entityManager;

    private final Class<T> type;

    private final DatasourceQuery query;

    @SuppressWarnings("unchecked")
    public <K> TypedQuery<K> prepareKeyQuery(String idFieldName)
    {
        try
        {
            Field idField = this.type.getDeclaredField(idFieldName);

            CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
            CriteriaQuery<K> query = criteriaBuilder.createQuery((Class<K>) idField.getType());
            Root<T> root = query.from(this.type);

            query.select(root.get(idFieldName))
                 .where(this.query.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
                 .orderBy(this.query.getSortedOrderings(criteriaBuilder, root));

            TypedQuery<K> typedQuery = this.entityManager.createQuery(query)
                                                         .setHint(HibernateHints.HINT_READ_ONLY, true);

            List<BiTuple<String, Object>> parameters = this.query.getParameters();
            parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

            DatasourcePaginationInfo pagination = this.query.getPagination().orElse(null);
            if (pagination != null)
            {
                pagination.getSize().ifPresent(typedQuery::setMaxResults);
                pagination.getOffset().ifPresent(typedQuery::setFirstResult);
            }

            return typedQuery;
        }
        catch (NoSuchFieldException exception)
        {
            throw new UnknownRelationException("Unable to find id field", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public <O> TypedQuery<O> prepareOptionQuery( )
    {
        // @formatter:off
        if (this.query.getProperties().size() > 1)
            log.warn("Fetching options for datasource-query with more than one property defined: " +
                    "Only the first property will be used for this query.");
        // @formatter:on

        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<O> query = (CriteriaQuery<O>) criteriaBuilder.createQuery();
        Root<T> root = query.from(this.type);

        String property = this.query.getFirstProperty().orElseThrow();
        query.select(root.get(property))
             .distinct(true)
             .where(this.query.getConditions(criteriaBuilder, root).toArray(Predicate[]::new));

        TypedQuery<O> typedQuery = this.entityManager.createQuery(query).setHint(HibernateHints.HINT_READ_ONLY, true);

        List<BiTuple<String, Object>> parameters = this.query.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        DatasourcePaginationInfo pagination = this.query.getPagination().orElse(null);
        if (pagination != null)
        {
            pagination.getSize().ifPresent(typedQuery::setMaxResults);
            pagination.getOffset().ifPresent(typedQuery::setFirstResult);

            if (pagination.includeTotal())
                pagination.setTotal(this.executeOptionCountQuery(parameters));
        }

        return typedQuery;
    }

    public TypedQuery<T> prepareDataQuery( )
    {
        if (this.query.getRelations().stream().noneMatch(DatasourceRelationInfo::isFetch))
            return this.prepareRegularDataQuery();

        Field idField = this.findIdField().orElse(null);
        if (idField == null)
            return this.prepareRegularDataQuery();

        List<?> keys = this.prepareKeyQuery(idField.getName()).getResultList();
        return this.prepareIdDataQuery(idField.getName(), keys);
    }

    @SuppressWarnings("rawtypes")
    private <K> TypedQuery<T> prepareIdDataQuery(String idFieldName, List<K> ids)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
        Root<T> root = query.from(this.type);

        Parameter<List> idParameter = criteriaBuilder.parameter(List.class);
        query.select(root)
             .where(root.get(idFieldName).in(idParameter))
             .orderBy(this.query.getSortedOrderings(criteriaBuilder, root));

        EntityGraph<T> graph = this.getRelationGraph();
        TypedQuery<T> typedQuery = this.entityManager.createQuery(query)
                                                     .setParameter(idParameter, ids)
                                                     .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                     .setHint(HibernateHints.HINT_READ_ONLY, true);

        DatasourcePaginationInfo pagination = this.query.getPagination().orElse(null);
        if (pagination != null && pagination.includeTotal())
            pagination.setTotal(this.executeCountQuery(graph, this.query.getParameters())
                                    .stream()
                                    .mapToLong(DatasourceCountInfo::getCount)
                                    .sum());

        return typedQuery;
    }

    private TypedQuery<T> prepareRegularDataQuery( )
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(this.type);
        Root<T> root = query.from(this.type);

        query.select(root)
             .where(this.query.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .orderBy(this.query.getSortedOrderings(criteriaBuilder, root));

        EntityGraph<T> graph = this.getRelationGraph();
        TypedQuery<T> typedQuery = this.entityManager.createQuery(query)
                                                     .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                     .setHint(HibernateHints.HINT_READ_ONLY, true);

        List<BiTuple<String, Object>> parameters = this.query.getParameters();
        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        DatasourcePaginationInfo pagination = this.query.getPagination().orElse(null);
        if (pagination != null)
        {
            pagination.getSize().ifPresent(typedQuery::setMaxResults);
            pagination.getOffset().ifPresent(typedQuery::setFirstResult);

            if (pagination.includeTotal())
                pagination.setTotal(this.executeCountQuery(graph, parameters)
                                        .stream()
                                        .mapToLong(DatasourceCountInfo::getCount)
                                        .sum());
        }

        return typedQuery;
    }

    public List<DatasourceCountInfo> executeCountQuery( )
    {
        return this.executeCountQuery(this.getRelationGraph(), this.query.getParameters());
    }

    private long executeOptionCountQuery(List<BiTuple<String, Object>> parameters)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<T> root = query.from(this.type);

        String property = this.query.getFirstProperty().orElseThrow();
        query.select(criteriaBuilder.countDistinct(root.get(property)))
             .where(this.query.getConditions(criteriaBuilder, root).toArray(Predicate[]::new));

        TypedQuery<Long> typedQuery = this.entityManager.createQuery(query);

        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        return typedQuery.getSingleResult();
    }

    private List<DatasourceCountInfo> executeCountQuery(EntityGraph<?> graph, List<BiTuple<String, Object>> parameters)
    {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
        Root<T> root = query.from(this.type);

        List<Path<?>> grouping = this.query.getGroupings(root);
        List<Selection<?>> selection = new ArrayList<>();
        selection.add(criteriaBuilder.count(root));
        selection.addAll(grouping);

        // FIXME: For multi-level data-query the grouping must be on primary-element id
        query.multiselect(selection)
             .where(this.query.getConditions(criteriaBuilder, root).toArray(Predicate[]::new))
             .groupBy(new ArrayList<>(grouping));

        TypedQuery<Object[]> typedQuery = this.entityManager.createQuery(query)
                                                            .setHint(SpecHints.HINT_SPEC_FETCH_GRAPH, graph)
                                                            .setHint(HibernateHints.HINT_READ_ONLY, true);

        parameters.forEach(parameter -> typedQuery.setParameter(parameter.first(), parameter.second()));

        return typedQuery.getResultList()
                         .stream()
                         .map(item -> DatasourceCountInfo.builder()
                                                         .count((long) item[ 0 ])
                                                         .groupKey(grouping.isEmpty() ? Collections.emptyList() :
                                                                 Arrays.asList(item).subList(1, item.length))
                                                         .build())
                         .toList();
    }

    private EntityGraph<T> getRelationGraph( )
    {
        EntityGraph<T> graph = this.entityManager.createEntityGraph(this.type);
        this.query.addRelations(graph);

        return graph;
    }

    private Optional<Field> findIdField( )
    {
        List<Field> idFields = Arrays.stream(this.type.getDeclaredFields())
                                     .filter(item -> item.isAnnotationPresent(Id.class))
                                     .toList();

        if (idFields.size() != 1)
            return Optional.empty();

        return Optional.of(idFields.get(0));
    }

    public static <T> DatasourceQueryInterpreter<T> of(Class<T> type, DatasourceQuery query)
    {
        return DatasourceQueryInterpreter.of(Arc.container().select(EntityManager.class).get(), type, query);
    }
}
