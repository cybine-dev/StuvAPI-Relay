package de.cybine.stuvapi.relay.data.room;

import de.cybine.stuvapi.relay.data.lecture.Lecture;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
@Builder(builderClassName = "Builder")
public class Room
{
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", nullable = false, unique = true)
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @ToString.Exclude
    @ManyToMany(mappedBy = "rooms")
    private Set<Lecture> lectures;

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Room room = (Room) o;
        return id != null && Objects.equals(id, room.id);
    }

    @Override
    public int hashCode( )
    {
        return getClass().hashCode();
    }
}