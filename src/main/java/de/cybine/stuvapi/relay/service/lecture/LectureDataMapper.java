package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.util.converter.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import lombok.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@RequiredArgsConstructor
public class LectureDataMapper implements EntityMapper<Lecture, LectureData>
{
    private final RoomService roomService;

    private final GenericDatasourceRepository<LectureEntity> lectureRepository = GenericDatasourceRepository.forType(
            LectureEntity.class);

    @Override
    public Class<Lecture> getEntityType( )
    {
        return Lecture.class;
    }

    @Override
    public Class<LectureData> getDataType( )
    {
        return LectureData.class;
    }

    @Override
    public Lecture toEntity(LectureData data, ConversionHelper helper)
    {
        DatasourceConditionDetail<Long> hasLectureId = DatasourceHelper.isEqual(LectureEntity_.LECTURE_ID,
                data.getId());

        DatasourceQuery lectureQuery = DatasourceQuery.builder().condition(DatasourceHelper.and(hasLectureId)).build();

        Optional<LectureEntity> persistentLecture = Optional.empty();
        if (helper.<Boolean>findContext("lecture-registered").orElse(true))
            persistentLecture = this.lectureRepository.fetchSingle(lectureQuery);

        return Lecture.builder()
                      .id(persistentLecture.map(item -> LectureId.of(item.getId())).orElseGet(LectureId::create))
                      .lectureId(data.getId())
                      .name(data.getName())
                      .course(data.getCourse().orElse(null))
                      .startsAt(data.getStartsAt().atZoneSameInstant(ZoneId.systemDefault()))
                      .endsAt(data.getEndsAt().atZoneSameInstant(ZoneId.systemDefault()))
                      .type(data.getType())
                      .status(persistentLecture.map(LectureEntity::getStatus).orElse(LectureStatus.ACTIVE))
                      .rooms(data.getRooms()
                                 .stream()
                                 .map(this.roomService::findIdByName)
                                 .map(Optional::orElseThrow)
                                 .map(item -> Room.builder().id(item).build())
                                 .collect(Collectors.toSet()))
                      .build();
    }

    @Override
    public LectureData toData(Lecture entity, ConversionHelper helper)
    {
        return LectureData.builder()
                          .id(entity.getLectureId())
                          .name(entity.getName())
                          .course(entity.getCourse().orElse(null))
                          .startsAt(entity.getStartsAt().toOffsetDateTime())
                          .endsAt(entity.getEndsAt().toOffsetDateTime())
                          .type(entity.getType())
                          .rooms(entity.getRooms()
                                       .orElseThrow()
                                       .stream()
                                       .map(Room::getName)
                                       .collect(Collectors.toSet()))
                          .build();
    }
}
