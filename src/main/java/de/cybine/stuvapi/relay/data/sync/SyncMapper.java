package de.cybine.stuvapi.relay.data.sync;

import de.cybine.stuvapi.relay.data.EntityMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@AllArgsConstructor
public class SyncMapper implements EntityMapper<Sync, SyncDto>
{
    private final LectureSyncMapper lectureSyncMapper;

    @Override
    public Sync toEntity(SyncDto data)
    {
        return Sync.builder()
                .id(data.getId().orElse(null))
                .startedAt(data.getStartedAt())
                .finishedAt(data.getFinishedAt())
                .data(data.getData()
                        .map(details -> details.stream().map(this.lectureSyncMapper::toEntity).toList())
                        .orElse(null))
                .build();
    }

    @Override
    public SyncDto toData(Sync entity)
    {
        return SyncDto.builder()
                .id(entity.getId())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .data(Hibernate.isInitialized(entity.getData()) ? entity.getData()
                        .stream()
                        .map(this.lectureSyncMapper::toData)
                        .toList() : null)
                .build();
    }
}
