package de.cybine.stuvapi.relay.util.api.converter;

import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;

public class ApiCountQueryConverter implements Converter<ApiCountQuery, DatasourceQuery>
{
    @Override
    public Class<ApiCountQuery> getInputType( )
    {
        return ApiCountQuery.class;
    }

    @Override
    public Class<DatasourceQuery> getOutputType( )
    {
        return DatasourceQuery.class;
    }

    @Override
    public DatasourceQuery convert(ApiCountQuery input, ConversionHelper helper)
    {
        return DatasourceQuery.builder()
                              .groupingProperties(input.getGroupingProperties()
                                                       .stream()
                                                       .map(item -> ApiQueryConverter.getFieldPathOrThrow(helper, item))
                                                       .map(DatasourceFieldPath::asString)
                                                       .toList())
                              .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                               .apply(input::getCondition))
                              .relations(helper.toList(ApiCountRelationInfo.class, DatasourceRelationInfo.class)
                                               .apply(input::getRelations))
                              .build();
    }
}
