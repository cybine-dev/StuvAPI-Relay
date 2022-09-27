package de.cybine.stuvapi.relay.data.sync;

import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Schema(name = "Sync")
@Builder(builderClassName = "SyncBuilder")
public class SyncDto
{
    private final UUID id;

    private final ZonedDateTime startedAt;
    private final ZonedDateTime finishedAt;

    private final List<LectureSync> data;

    public Optional<UUID> getId( )
    {
        return Optional.ofNullable(this.id);
    }

    public Optional<List<LectureSync>> getData( )
    {
        return Optional.ofNullable(this.data);
    }

    @Data
    @Schema(name = "LectureSync")
    @Builder(builderClassName = "Builder")
    public static class LectureSync
    {
        private final UUID id;
        private final UUID syncId;

        private final Type type;

        private final LectureDto lecture;

        private List<SyncDetail> details;

        public Optional<UUID> getId( )
        {
            return Optional.ofNullable(this.id);
        }

        public Optional<UUID> getSyncId( )
        {
            return Optional.ofNullable(this.syncId);
        }

        public Optional<LectureDto> getLecture( )
        {
            return Optional.ofNullable(this.lecture);
        }
    }

    @Data
    @Schema(name = "SyncDetail")
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
    @Schema(name = "SyncType")
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