package de.cybine.stuvapi.relay.data.action.metadata;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.util.*;

@UtilityClass
@StaticMetamodel(ActionMetadataEntity.class)
public class ActionMetadataEntity_
{
    public static final String TABLE  = "action_metadata";
    public static final String ENTITY = "ActionMetadata";

    public static final String ID_COLUMN        = "id";
    public static final String NAMESPACE_COLUMN = "namespace";
    public static final String CATEGORY_COLUMN  = "category";
    public static final String NAME_COLUMN      = "name";
    public static final String TYPE_COLUMN      = "type";

    // @formatter:off
    public static final DatasourceField ID        =
            DatasourceField.property(ActionMetadataEntity.class, "id", UUID.class);
    public static final DatasourceField NAMESPACE =
            DatasourceField.property(ActionMetadataEntity.class, "namespace", String.class);
    public static final DatasourceField CATEGORY =
            DatasourceField.property(ActionMetadataEntity.class, "category", String.class);
    public static final DatasourceField NAME =
            DatasourceField.property(ActionMetadataEntity.class, "name", String.class);
    public static final DatasourceField TYPE =
            DatasourceField.property(ActionMetadataEntity.class, "type", String.class);
    public static final DatasourceField CONTEXTS =
            DatasourceField.property(ActionMetadataEntity.class, "contexts", ActionContextEntity.class);
    // @formatter:on

    public static volatile SingularAttribute<ActionMetadataEntity, UUID>           id;
    public static volatile SingularAttribute<ActionMetadataEntity, String>         namespace;
    public static volatile SingularAttribute<ActionMetadataEntity, String>         category;
    public static volatile SingularAttribute<ActionMetadataEntity, String>         name;
    public static volatile SingularAttribute<ActionMetadataEntity, String>         type;
    public static volatile SetAttribute<ActionMetadataEntity, ActionContextEntity> contexts;
}
