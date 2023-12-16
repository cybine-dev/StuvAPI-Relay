package de.cybine.stuvapi.relay.data.action.process;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.time.*;
import java.util.*;

@UtilityClass
@SuppressWarnings("unused")
@StaticMetamodel(ActionProcessEntity.class)
public class ActionProcessEntity_
{
    public static final String TABLE  = "action_process";
    public static final String ENTITY = "ActionProcess";

    public static final String ID_COLUMN          = "id";
    public static final String EVENT_ID_COLUMN    = "event_id";
    public static final String CONTEXT_ID_COLUMN  = "context_id";
    public static final String STATUS_COLUMN      = "status";
    public static final String PRIORITY_COLUMN    = "priority";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String CREATOR_ID_COLUMN  = "creator_id";
    public static final String CREATED_AT_COLUMN  = "created_at";
    public static final String DUE_AT_COLUMN      = "due_at";
    public static final String DATA_COLUMN        = "data";

    // @formatter:off
    public static final DatasourceField ID          =
            DatasourceField.property(ActionProcessEntity.class, "id", UUID.class);
    public static final DatasourceField EVENT_ID    =
            DatasourceField.property(ActionProcessEntity.class, "eventId", String.class);
    public static final DatasourceField CONTEXT_ID  =
            DatasourceField.property(ActionProcessEntity.class, "contextId", UUID.class);
    public static final DatasourceField CONTEXT     =
            DatasourceField.property(ActionProcessEntity.class, "context", ActionContextEntity.class);
    public static final DatasourceField STATUS      =
            DatasourceField.property(ActionProcessEntity.class, "status", String.class);
    public static final DatasourceField PRIORITY    =
            DatasourceField.property(ActionProcessEntity.class, "priority", Integer.class);
    public static final DatasourceField DESCRIPTION =
            DatasourceField.property(ActionProcessEntity.class, "description", String.class);
    public static final DatasourceField CREATOR_ID  =
            DatasourceField.property(ActionProcessEntity.class, "creatorId", String.class);
    public static final DatasourceField CREATED_AT  =
            DatasourceField.property(ActionProcessEntity.class, "createdAt", ZonedDateTime.class);
    public static final DatasourceField DUE_AT      =
            DatasourceField.property(ActionProcessEntity.class, "dueAt", ZonedDateTime.class);
    public static final DatasourceField DATA        =
            DatasourceField.property(ActionProcessEntity.class, "data", String.class);
    // @formatter:on

    public static final String CONTEXT_RELATION = "context";

    public static volatile SingularAttribute<ActionContextEntity, UUID>                id;
    public static volatile SingularAttribute<ActionContextEntity, String>              eventId;
    public static volatile SingularAttribute<ActionContextEntity, UUID>                contextId;
    public static volatile SingularAttribute<ActionContextEntity, ActionContextEntity> context;
    public static volatile SingularAttribute<ActionContextEntity, String>              status;
    public static volatile SingularAttribute<ActionContextEntity, Integer>             priority;
    public static volatile SingularAttribute<ActionContextEntity, String>              description;
    public static volatile SingularAttribute<ActionContextEntity, String>              creatorId;
    public static volatile SingularAttribute<ActionContextEntity, ZonedDateTime>       createdAt;
    public static volatile SingularAttribute<ActionContextEntity, ZonedDateTime>       dueAt;
    public static volatile SingularAttribute<ActionContextEntity, String>              data;
}
