package de.cybine.stuvapi.relay.data.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cybine.stuvapi.relay.data.EntityConversionException;
import de.cybine.stuvapi.relay.data.EntityMapper;
import de.cybine.stuvapi.relay.data.lecture.LectureMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class LectureSyncMapper implements EntityMapper<Sync.LectureSync, SyncDto.LectureSync>
{
    private final ObjectMapper objectMapper;

    private final LectureMapper lectureMapper;

    @Override
    public Sync.LectureSync toEntity(SyncDto.LectureSync data)
    {
        try
        {
            return Sync.LectureSync.builder()
                    .id(data.getId().orElse(null))
                    .sync(data.getSyncId().map(id -> Sync.builder().id(id).build()).orElse(null))
                    .lecture(data.getLecture().map(this.lectureMapper::toEntity).orElse(null))
                    .type(data.getType().getTypeId())
                    .details(this.objectMapper.writeValueAsString(data.getDetails()))
                    .build();
        }
        catch (JsonProcessingException exception)
        {
            throw new EntityConversionException("Could not convert lecture data to lecture", exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SyncDto.LectureSync toData(final Sync.LectureSync entity)
    {
        try
        {
            return SyncDto.LectureSync.builder()
                    .id(entity.getId())
                    .syncId(Hibernate.isInitialized(entity.getSync()) ? entity.getSync().getId() : null)
                    .lecture(Hibernate.isInitialized(entity.getLecture()) ? this.lectureMapper.toData(entity.getLecture()) : null)
                    .type(SyncDto.Type.getByTypeId(entity.getType()))
                    .details(this.objectMapper.readValue(entity.getDetails(), List.class))
                    .build();
        }
        catch (JsonProcessingException exception)
        {
            throw new EntityConversionException("Could not convert lecture data to lecture", exception);
        }
    }
}
