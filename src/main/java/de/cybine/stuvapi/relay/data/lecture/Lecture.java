package de.cybine.stuvapi.relay.data.lecture;

import de.cybine.stuvapi.relay.data.room.Room;
import de.cybine.stuvapi.relay.data.sync.Sync;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lectures")
@Builder(builderClassName = "Builder")
public class Lecture
{
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", nullable = false, unique = true)
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "lecture_id", nullable = false, unique = true)
    private long lectureId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "course")
    private String course;

    @Column(name = "lecturer")
    private String lecturer;

    @Column(name = "type", nullable = false)
    private int type;

    @Column(name = "archived", nullable = false)
    private boolean isArchived;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @ManyToMany
    @JoinTable(name = "lecture_rooms",
               joinColumns = { @JoinColumn(name = "lecture_id") },
               inverseJoinColumns = { @JoinColumn(name = "room_id") })
    private Set<Room> rooms;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "lecture")
    private List<Sync.LectureSync> syncs;
}
