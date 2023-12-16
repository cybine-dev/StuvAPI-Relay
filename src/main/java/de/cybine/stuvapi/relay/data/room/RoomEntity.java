package de.cybine.stuvapi.relay.data.room;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = RoomEntity_.TABLE)
@Entity(name = RoomEntity_.ENTITY)
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = RoomEntity_.ID_COLUMN, nullable = false)
    private UUID id;

    @Column(name = RoomEntity_.NAME_COLUMN, nullable = false, unique = true)
    private String name;

    @Column(name = RoomEntity_.DISPLAY_NAME_COLUMN)
    private String displayName;

    @ManyToMany(mappedBy = "rooms")
    private Set<LectureEntity> lectures;

    public Optional<String> getDisplayName( )
    {
        return Optional.ofNullable(this.displayName);
    }

    public Optional<Set<LectureEntity>> getLectures( )
    {
        return Optional.ofNullable(this.lectures);
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