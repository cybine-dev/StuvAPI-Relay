package de.cybine.stuvapi.relay.data.sync;

import de.cybine.stuvapi.relay.data.lecture.Lecture;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "syncs")
@Builder(builderClassName = "SyncBuilder")
public class Sync
{
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", nullable = false, unique = true)
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sync")
    private List<LectureSync> data;

    @Data
    @Entity
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "sync_data")
    @Builder(builderClassName = "Builder")
    public static class LectureSync
    {
        @Id
        @GeneratedValue(generator = "UUID")
        @Column(name = "id", nullable = false, unique = true)
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        private UUID id;

        @ManyToOne
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        @JoinColumn(name = "sync_id", nullable = false)
        private Sync sync;

        @ManyToOne
        @JoinColumn(name = "lecture_id")
        private Lecture lecture;

        @Column(name = "type", nullable = false)
        private int type;

        @Column(name = "data", nullable = false)
        private String details;
    }
}
