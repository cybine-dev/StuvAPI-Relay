package de.cybine.stuvapi.relay.data.action.context;

import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.stuvapi.relay.data.action.process.*;

public class ActionContextMapper implements EntityMapper<ActionContextEntity, ActionContext>
{
    @Override
    public Class<ActionContextEntity> getEntityType( )
    {
        return ActionContextEntity.class;
    }

    @Override
    public Class<ActionContext> getDataType( )
    {
        return ActionContext.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(ActionProcess.class, ActionProcessEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(ActionProcessEntity.class, ActionProcess.class);
    }

    @Override
    public ActionContextEntity toEntity(ActionContext data, ConversionHelper helper)
    {
        return ActionContextEntity.builder()
                                  .id(data.findId().map(Id::getValue).orElse(null))
                                  .namespace(data.getNamespace())
                                  .category(data.getCategory())
                                  .name(data.getName())
                                  .correlationId(data.getCorrelationId())
                                  .itemId(data.getItemId().orElse(null))
                                  .processes(helper.toSet(ActionProcess.class, ActionProcessEntity.class)
                                                   .map(data::getProcesses))
                                  .build();
    }

    @Override
    public ActionContext toData(ActionContextEntity entity, ConversionHelper helper)
    {
        return ActionContext.builder()
                            .id(ActionContextId.of(entity.getId()))
                            .namespace(entity.getNamespace())
                            .category(entity.getCategory())
                            .name(entity.getName())
                            .correlationId(entity.getCorrelationId())
                            .itemId(entity.getItemId().orElse(null))
                            .processes(helper.toSet(ActionProcessEntity.class, ActionProcess.class)
                                             .map(entity::getProcesses))
                            .build();
    }
}
