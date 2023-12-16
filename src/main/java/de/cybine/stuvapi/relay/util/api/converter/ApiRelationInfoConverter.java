package de.cybine.stuvapi.relay.util.api.converter;

import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.exception.datasource.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import lombok.*;

@RequiredArgsConstructor
public class ApiRelationInfoConverter implements Converter<ApiRelationInfo, DatasourceRelationInfo>
{
    private final ApplicationConfig config;

    @Override
    public Class<ApiRelationInfo> getInputType( )
    {
        return ApiRelationInfo.class;
    }

    @Override
    public Class<DatasourceRelationInfo> getOutputType( )
    {
        return DatasourceRelationInfo.class;
    }

    @Override
    public DatasourceRelationInfo convert(ApiRelationInfo input, ConversionHelper helper)
    {
        if(input.getRelations() != null && !input.getRelations().isEmpty() && !this.allowMultiLevelRelations())
            throw new UnknownRelationException("Cannot reference multiple relation levels");

        DatasourceFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper, input.getProperty());
        if (path.getLength() > 1)
            throw new UnknownRelationException(
                    String.format("Cannot traverse multiple elements while resolving relations (%s)",
                            input.getProperty()));

        helper.withContext(ApiQueryConverter.DATA_TYPE_PROPERTY, path.getLast().getFieldType());

        return DatasourceRelationInfo.builder()
                                     .property(path.asString())
                                     .fetch(input.isFetch())
                                     .condition(helper.toItem(ApiConditionInfo.class, DatasourceConditionInfo.class)
                                                      .map(input::getCondition))
                                     .order(helper.toList(ApiOrderInfo.class, DatasourceOrderInfo.class)
                                                  .apply(input::getOrder))
                                     .relations(helper.toList(ApiRelationInfo.class, DatasourceRelationInfo.class)
                                                      .apply(input::getRelations))
                                     .build();
    }

    private boolean allowMultiLevelRelations()
    {
        return this.config.converter().allowMultiLevelRelations();
    }
}
