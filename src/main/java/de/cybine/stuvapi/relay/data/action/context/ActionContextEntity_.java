package de.cybine.stuvapi.relay.data.action.context;

import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.util.*;

@UtilityClass
@StaticMetamodel(ActionContextEntity.class)
public class ActionContextEntity_
{
    public static final String TABLE  = "action_context";
    public static final String ENTITY = "ActionContext";

    public static final String ID_COLUMN             = "id";
    public static final String NAMESPACE_COLUMN      = "namespace";
    public static final String CATEGORY_COLUMN       = "category";
    public static final String NAME_COLUMN           = "name";
    public static final String CORRELATION_ID_COLUMN = "correlation_id";
    public static final String ITEM_ID_COLUMN        = "item_id";

    // @formatter:off
    public static final DatasourceField ID        =
            DatasourceField.property(ActionContextEntity.class, "id", UUID.class);
    public static final DatasourceField NAMESPACE =
            DatasourceField.property(ActionContextEntity.class, "namespace", String.class);
    public static final DatasourceField CATEGORY  =
            DatasourceField.property(ActionContextEntity.class, "category", String.class);
    public static final DatasourceField NAME      =
            DatasourceField.property(ActionContextEntity.class, "name", String.class);
    public static final DatasourceField CORRELATION_ID =
            DatasourceField.property(ActionContextEntity.class, "correlationId", String.class);
    public static final DatasourceField ITEM_ID   =
            DatasourceField.property(ActionContextEntity.class, "itemId", String.class);
    public static final DatasourceField PROCESSES =
            DatasourceField.property(ActionContextEntity.class, "processes", ActionProcessEntity.class);
    // @formatter:on

    public static final String METADATA_RELATION = "metadata";

    public static volatile SingularAttribute<ActionContextEntity, UUID>           id;
    public static volatile SingularAttribute<ActionContextEntity, String>         namespace;
    public static volatile SingularAttribute<ActionContextEntity, String>         category;
    public static volatile SingularAttribute<ActionContextEntity, String>         name;
    public static volatile SingularAttribute<ActionContextEntity, String>         correlationId;
    public static volatile SingularAttribute<ActionContextEntity, String>         itemId;
    public static volatile SetAttribute<ActionContextEntity, ActionProcessEntity> processes;
}
