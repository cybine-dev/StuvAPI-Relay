package de.cybine.stuvapi.relay.data.room;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.util.*;

@UtilityClass
@StaticMetamodel(RoomEntity.class)
public class RoomEntity_
{
    public static final String TABLE  = "rooms";
    public static final String ENTITY = "Room";

    public static final String ID_COLUMN           = "id";
    public static final String NAME_COLUMN         = "name";
    public static final String DISPLAY_NAME_COLUMN = "display_name";

    // @formatter:off
    public static final DatasourceField ID           =
            DatasourceField.property(RoomEntity.class, "id", UUID.class);
    public static final DatasourceField NAME         =
            DatasourceField.property(RoomEntity.class, "name", String.class);
    public static final DatasourceField DISPLAY_NAME =
            DatasourceField.property(RoomEntity.class, "displayName", String.class);
    public static final DatasourceField LECTURES     =
            DatasourceField.property(RoomEntity.class, "lectures", LectureEntity.class);
    // @formatter:on

    public static final String LECTURES_RELATION = "lectures";

    public static volatile SingularAttribute<RoomEntity, UUID>     id;
    public static volatile SingularAttribute<RoomEntity, String>   name;
    public static volatile SingularAttribute<RoomEntity, String>   displayName;
    public static volatile SetAttribute<RoomEntity, LectureEntity> lectures;
}
