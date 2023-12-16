package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = LectureEntity_.TABLE)
@Entity(name = LectureEntity_.ENTITY)
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LectureEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = LectureEntity_.ID_COLUMN, nullable = false)
    private UUID id;

    @Column(name = LectureEntity_.LECTURE_ID_COLUMN, nullable = false)
    private long lectureId;

    @Column(name = LectureEntity_.NAME_COLUMN, nullable = false)
    private String name;

    @Column(name = LectureEntity_.COURSE_COLUMN)
    private String course;

    @Column(name = LectureEntity_.STARTS_AT_COLUMN, nullable = false)
    private ZonedDateTime startsAt;

    @Column(name = LectureEntity_.ENDS_AT_COLUMN, nullable = false)
    private ZonedDateTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = LectureEntity_.TYPE_COLUMN, nullable = false)
    private LectureType type;

    @Enumerated(EnumType.STRING)
    @Column(name = LectureEntity_.STATUS_COLUMN, nullable = false)
    private LectureStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = LectureRoomRelation_.TABLE,
               joinColumns = @JoinColumn(name = LectureRoomRelation_.LECTURE_ID_COLUMN),
               inverseJoinColumns = @JoinColumn(name = LectureRoomRelation_.ROOM_ID_COLUMN))
    private Set<RoomEntity> rooms;

    public Optional<String> getCourse( )
    {
        return Optional.ofNullable(this.course);
    }

    public Optional<Set<RoomEntity>> getRooms( )
    {
        return Optional.ofNullable(this.rooms);
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == null)
            return false;

        if(this.getClass() != other.getClass())
            return false;

        WithId<?> that = ((WithId<?>) other);
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}
