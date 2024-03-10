package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.data.room.*;
import jakarta.persistence.metamodel.*;
import lombok.experimental.*;

import java.time.*;
import java.util.*;

@UtilityClass
@StaticMetamodel(LectureEntity.class)
public class LectureEntity_
{
    public static final String TABLE  = "lectures";
    public static final String ENTITY = "Lecture";

    public static final String ID_COLUMN         = "id";
    public static final String LECTURE_ID_COLUMN = "lecture_id";
    public static final String NAME_COLUMN       = "name";
    public static final String COURSE_COLUMN     = "course";
    public static final String STARTS_AT_COLUMN  = "starts_at";
    public static final String ENDS_AT_COLUMN    = "ends_at";
    public static final String TYPE_COLUMN       = "type";
    public static final String STATUS_COLUMN     = "status";

    // @formatter:off
    public static final DatasourceField ID         =
            DatasourceField.property(LectureEntity.class, "id", UUID.class);
    public static final DatasourceField LECTURE_ID =
            DatasourceField.property(LectureEntity.class, "lectureId", Long.class);
    public static final DatasourceField NAME       =
            DatasourceField.property(LectureEntity.class, "name", String.class);
    public static final DatasourceField COURSE     =
            DatasourceField.property(LectureEntity.class, "course", String.class);
    public static final DatasourceField STARTS_AT  =
            DatasourceField.property(LectureEntity.class, "startsAt", ZonedDateTime.class);
    public static final DatasourceField ENDS_AT    =
            DatasourceField.property(LectureEntity.class, "endsAt", ZonedDateTime.class);
    public static final DatasourceField TYPE       =
            DatasourceField.property(LectureEntity.class, "type", LectureType.class);
    public static final DatasourceField STATUS     =
            DatasourceField.property(LectureEntity.class, "status", LectureStatus.class);
    public static final DatasourceField ROOMS      =
            DatasourceField.property(LectureEntity.class, "rooms", RoomEntity.class);
    // @formatter:on

    public static volatile SingularAttribute<LectureEntity, UUID>          id;
    public static volatile SingularAttribute<LectureEntity, Long>          lectureId;
    public static volatile SingularAttribute<LectureEntity, String>        name;
    public static volatile SingularAttribute<LectureEntity, String>        course;
    public static volatile SingularAttribute<LectureEntity, ZonedDateTime> startsAt;
    public static volatile SingularAttribute<LectureEntity, ZonedDateTime> endsAt;
    public static volatile SingularAttribute<LectureEntity, LectureType>   type;
    public static volatile SingularAttribute<LectureEntity, LectureStatus> status;
    public static volatile SetAttribute<LectureEntity, RoomEntity>         rooms;
}
