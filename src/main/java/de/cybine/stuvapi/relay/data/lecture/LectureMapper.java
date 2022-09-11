package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.stuvapi.relay.data.EntityMapper;
import de.cybine.stuvapi.relay.data.room.RoomMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;

import javax.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;

@ApplicationScoped
@AllArgsConstructor
public class LectureMapper implements EntityMapper<Lecture, LectureDto>
{
    private final RoomMapper roomMapper;

    public Lecture toEntity(LectureDto data)
    {
        return Lecture.builder()
                .id(data.getId().orElse(null))
                .lectureId(data.getLectureId())
                .name(data.getName())
                .course(data.getCourse().orElse(null))
                .lecturer(data.getLecturer().orElse(null))
                .type(data.getType().getTypeId())
                .isArchived(data.isArchived())
                .createdAt(data.getCreatedAt())
                .updatedAt(data.getUpdatedAt())
                .startsAt(data.getStartsAt())
                .endsAt(data.getEndsAt())
                .rooms(data.getRooms()
                        .map(rooms -> rooms.stream().map(this.roomMapper::toEntity).collect(Collectors.toSet()))
                        .orElse(null))
                .build();
    }

    public LectureDto toData(Lecture entity)
    {
        return LectureDto.builder()
                .id(entity.getId())
                .lectureId(entity.getLectureId())
                .name(entity.getName())
                .course(entity.getCourse())
                .lecturer(entity.getLecturer())
                .type(LectureDto.Type.getByTypeId(entity.getType()))
                .isArchived(entity.isArchived())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .rooms(Hibernate.isInitialized(entity.getRooms()) ? entity.getRooms()
                        .stream()
                        .map(this.roomMapper::toData)
                        .collect(Collectors.toSet()) : null)
                .build();
    }
}
