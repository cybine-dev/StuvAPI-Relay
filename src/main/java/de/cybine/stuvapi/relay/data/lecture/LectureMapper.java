package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.data.util.primitive.*;
import de.cybine.stuvapi.relay.util.converter.*;

public class LectureMapper implements EntityMapper<LectureEntity, Lecture>
{
    @Override
    public Class<LectureEntity> getEntityType( )
    {
        return LectureEntity.class;
    }

    @Override
    public Class<Lecture> getDataType( )
    {
        return Lecture.class;
    }

    @Override
    public LectureEntity toEntity(Lecture data, ConversionHelper helper)
    {
        return LectureEntity.builder()
                            .id(data.findId().map(Id::getValue).orElse(null))
                            .lectureId(data.getLectureId())
                            .name(data.getName())
                            .course(data.getCourse().orElse(null))
                            .startsAt(data.getStartsAt())
                            .endsAt(data.getEndsAt())
                            .type(data.getType())
                            .status(data.getStatus())
                            .rooms(helper.toSet(Room.class, RoomEntity.class).map(data::getRooms))
                            .build();
    }

    @Override
    public Lecture toData(LectureEntity entity, ConversionHelper helper)
    {
        return Lecture.builder()
                      .id(LectureId.of(entity.getId()))
                      .lectureId(entity.getLectureId())
                      .name(entity.getName())
                      .course(entity.getCourse().orElse(null))
                      .startsAt(entity.getStartsAt())
                      .endsAt(entity.getEndsAt())
                      .type(entity.getType())
                      .status(entity.getStatus())
                      .rooms(helper.toSet(RoomEntity.class, Room.class).map(entity::getRooms))
                      .build();
    }
}
