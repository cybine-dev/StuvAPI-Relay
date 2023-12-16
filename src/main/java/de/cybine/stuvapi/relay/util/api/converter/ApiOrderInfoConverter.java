package de.cybine.stuvapi.relay.util.api.converter;

import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;

public class ApiOrderInfoConverter implements Converter<ApiOrderInfo, DatasourceOrderInfo>
{
    @Override
    public Class<ApiOrderInfo> getInputType( )
    {
        return ApiOrderInfo.class;
    }

    @Override
    public Class<DatasourceOrderInfo> getOutputType( )
    {
        return DatasourceOrderInfo.class;
    }

    @Override
    public DatasourceOrderInfo convert(ApiOrderInfo input, ConversionHelper helper)
    {
        DatasourceFieldPath path = ApiQueryConverter.getFieldPathOrThrow(helper, input.getProperty());
        return DatasourceOrderInfo.builder()
                                  .property(path.asString())
                                  .priority(input.getPriority())
                                  .isAscending(input.isAscending())
                                  .build();
    }
}
