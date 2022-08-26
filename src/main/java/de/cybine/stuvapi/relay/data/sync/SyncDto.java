package de.cybine.stuvapi.relay.data.sync;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder(builderClassName = "SyncBuilder")
public class SyncDto
{
    private final UUID id;

    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    private final List<LectureSync> data;

    public Optional<UUID> getId( )
    {
        return Optional.ofNullable(this.id);
    }

    @Data
    @Builder(builderClassName = "Builder")
    public static class LectureSync
    {
        private final UUID id;
        private final UUID syncId;
        private final UUID lectureId;

        private final Type type;

        private List<SyncDetail> details;

        public Optional<UUID> getId( )
        {
            return Optional.ofNullable(this.id);
        }

        public Optional<UUID> getSyncId( )
        {
            return Optional.ofNullable(this.syncId);
        }

        public Optional<UUID> getLectureId( )
        {
            return Optional.ofNullable(this.lectureId);
        }
    }

    @Data
    @Builder(builderClassName = "Builder")
    public static class SyncDetail
    {
        private final String description;
        private final String fieldName;

        private final String previousValue;
        private final String currentValue;
    }

    @Getter
    @AllArgsConstructor
    public enum Type
    {
        CREATED(1), UPDATED(2), DELETED(3);

        private final int typeId;

        public static Type getByTypeId(int typeId)
        {
            return Arrays.stream(Type.values()).filter(type -> type.getTypeId() == typeId).findAny().orElse(null);
        }
    }
}