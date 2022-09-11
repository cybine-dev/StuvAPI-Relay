package de.cybine.stuvapi.relay.api.v1.result;

import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import de.cybine.stuvapi.relay.data.sync.SyncDto;

import java.util.Collection;
import java.util.UUID;

public record SyncInfo(UUID id,
                       UUID syncId,
                       SyncDto.Type type,
                       LectureDto lecture,
                       Collection<SyncDto.SyncDetail> details)
{ }
