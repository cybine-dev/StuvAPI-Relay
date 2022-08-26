package de.cybine.stuvapi.relay.data.sync;

import de.cybine.stuvapi.relay.data.EntityMapper;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@AllArgsConstructor
public class SyncMapper implements EntityMapper<Sync, SyncDto>
{
    private final LectureSyncMapper lectureSyncMapper;

    @Override
    public Sync toEntity(final SyncDto data)
    {
        return Sync.builder()
                .id(data.getId().orElse(null))
                .startedAt(data.getStartedAt())
                .finishedAt(data.getFinishedAt())
                .data(data.getData().stream().map(this.lectureSyncMapper::toEntity).toList())
                .build();
    }

    @Override
    public SyncDto toData(final Sync entity)
    {
        return SyncDto.builder()
                .id(entity.getId())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .data(entity.getData().stream().map(this.lectureSyncMapper::toData).toList())
                .build();
    }
}
