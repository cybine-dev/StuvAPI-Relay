package de.cybine.stuvapi.relay.util.datasource;

import de.cybine.stuvapi.relay.util.api.*;
import de.cybine.stuvapi.relay.util.api.converter.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.converter.*;
import io.quarkus.arc.*;
import jakarta.enterprise.inject.*;
import jakarta.transaction.*;
import lombok.*;

import java.util.*;

@SuppressWarnings("unused")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GenericDatasourceService<E, D>
{
    private final Class<E> entityType;
    private final Class<D> dataType;

    private final ApiFieldResolver  fieldResolver;
    private final ConverterRegistry registry;

    private final GenericDatasourceRepository<E> repository;

    private final Instance<ApiPaginationInfo> pagination;

    public List<D> fetch(ApiQuery query)
    {
        return this.fetch(this.getDatasourceQuery(query));
    }

    public List<D> fetch(DatasourceQuery query)
    {
        List<E> entities = this.fetchEntities(query);
        query.getPagination().ifPresent(this::applyPagination);

        return this.getToDataProcessor().toList(entities).result();
    }

    public List<E> fetchEntities(DatasourceQuery query)
    {
        return this.repository.fetch(query);
    }

    public Optional<D> fetchSingle(ApiQuery query)
    {
        return this.fetchSingle(this.getDatasourceQuery(query));
    }

    public Optional<D> fetchSingle(DatasourceQuery query)
    {
        return this.fetchSingleEntity(query).map(this.getToDataProcessor()::toItem).map(ConversionResult::result);
    }

    @Transactional
    public Optional<E> fetchSingleEntity(DatasourceQuery query)
    {
        return this.repository.fetchSingle(query);
    }

    public <O> List<O> fetchOptions(ApiOptionQuery query)
    {
        return this.fetchOptions(this.getDatasourceQuery(query));
    }

    public <O> List<O> fetchOptions(DatasourceQuery query)
    {
        List<O> options = this.repository.fetchOptions(query);
        query.getPagination().ifPresent(this::applyPagination);

        return options;
    }

    public List<ApiCountInfo> fetchTotal(ApiCountQuery query)
    {
        return this.registry.getProcessor(DatasourceCountInfo.class, ApiCountInfo.class)
                            .toList(this.fetchTotal(this.getDatasourceQuery(query)))
                            .result();
    }

    public List<DatasourceCountInfo> fetchTotal(DatasourceQuery query)
    {
        return this.repository.fetchTotal(query);
    }

    private DatasourceQuery getDatasourceQuery(ApiQuery query)
    {
        return this.getDatasourceQuery(ApiQuery.class, query);
    }

    private DatasourceQuery getDatasourceQuery(ApiOptionQuery query)
    {
        return this.getDatasourceQuery(ApiOptionQuery.class, query);
    }

    private DatasourceQuery getDatasourceQuery(ApiCountQuery query)
    {
        return this.getDatasourceQuery(ApiCountQuery.class, query);
    }

    private <T> DatasourceQuery getDatasourceQuery(Class<T> type, T query)
    {
        return this.getDatasourceQuery(type, query, this.fieldResolver.getUserContext().getContextName());
    }

    private <T> DatasourceQuery getDatasourceQuery(Class<T> type, T query, String context)
    {
        ConverterConstraint constraint = ConverterConstraint.builder().allowEmptyCollection(true).maxDepth(20).build();
        ConverterTree tree = ConverterTree.builder().constraint(constraint).build();

        System.out.println(context);

        return this.registry.getProcessor(type, DatasourceQuery.class, tree)
                            .withContext(ApiQueryConverter.CONTEXT_PROPERTY, context)
                            .withContext(ApiQueryConverter.DATA_TYPE_PROPERTY, this.dataType)
                            .toItem(query)
                            .result();
    }

    private ConversionProcessor<E, D> getToDataProcessor( )
    {
        return this.registry.getProcessor(this.entityType, this.dataType);
    }

    private void applyPagination(DatasourcePaginationInfo pagination)
    {
        if (pagination == null || this.pagination.isAmbiguous() || this.pagination.isUnsatisfied())
            return;

        ApiPaginationInfo apiPagination = this.pagination.get();
        pagination.getSize().ifPresent(apiPagination::setSize);
        pagination.getOffset().ifPresent(apiPagination::setOffset);
        pagination.getTotal().ifPresent(apiPagination::setTotal);
    }

    public static <E, D> GenericDatasourceService<E, D> forType(Class<E> entityType, Class<D> dataType)
    {
        ApiFieldResolver fieldResolver = Arc.container().select(ApiFieldResolver.class).get();
        ConverterRegistry registry = Arc.container().select(ConverterRegistry.class).get();

        return GenericDatasourceService.forType(entityType, dataType, fieldResolver, registry);
    }

    public static <E, D> GenericDatasourceService<E, D> forType(Class<E> entityType, Class<D> dataType,
            ApiFieldResolver fieldResolver, ConverterRegistry registry)
    {
        GenericDatasourceRepository<E> repository = GenericDatasourceRepository.forType(entityType);
        InjectableInstance<ApiPaginationInfo> pagination = Arc.container().select(ApiPaginationInfo.class);

        return new GenericDatasourceService<>(entityType, dataType, fieldResolver, registry, repository, pagination);
    }
}
