package de.cybine.stuvapi.relay.data.room;

import de.cybine.quarkus.data.util.primitive.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.stuvapi.relay.data.lecture.*;

public class RoomMapper implements EntityMapper<RoomEntity, Room>
{
    @Override
    public Class<RoomEntity> getEntityType( )
    {
        return RoomEntity.class;
    }

    @Override
    public Class<Room> getDataType( )
    {
        return Room.class;
    }

    @Override
    public ConverterMetadataBuilder getToEntityMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(Lecture.class, LectureEntity.class);
    }

    @Override
    public ConverterMetadataBuilder getToDataMetadata(ConverterMetadataBuilder metadata)
    {
        return metadata.withRelation(LectureEntity.class, Lecture.class);
    }

    @Override
    public RoomEntity toEntity(Room data, ConversionHelper helper)
    {
        return RoomEntity.builder()
                         .id(data.findId().map(Id::getValue).orElse(null))
                         .name(data.getName())
                         .displayName(helper.optional(data::getDisplayName)
                                            .filter(item -> !item.equals(data.getName()))
                                            .orElse(null))
                         .lectures(helper.toSet(Lecture.class, LectureEntity.class).map(data::getLectures))
                         .build();
    }

    @Override
    public Room toData(RoomEntity entity, ConversionHelper helper)
    {
        return Room.builder()
                   .id(RoomId.of(entity.getId()))
                   .name(entity.getName())
                   .displayName(entity.getDisplayName().orElse(null))
                   .lectures(helper.toSet(LectureEntity.class, Lecture.class).map(entity::getLectures))
                   .build();
    }
}
