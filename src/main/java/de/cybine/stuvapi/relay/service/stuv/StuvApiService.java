package de.cybine.stuvapi.relay.service.stuv;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.cybine.stuvapi.relay.data.lecture.Lecture;
import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import de.cybine.stuvapi.relay.data.lecture.LectureMapper;
import de.cybine.stuvapi.relay.data.room.Room;
import de.cybine.stuvapi.relay.data.room.RoomDto;
import de.cybine.stuvapi.relay.data.room.RoomMapper;
import de.cybine.stuvapi.relay.data.sync.LectureSyncMapper;
import de.cybine.stuvapi.relay.data.sync.Sync;
import de.cybine.stuvapi.relay.data.sync.SyncDto;
import de.cybine.stuvapi.relay.data.sync.SyncMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@ApplicationScoped
@AllArgsConstructor
public class StuvApiService
{
    private final StuvApi stuvApi;

    private final SessionFactory sessionFactory;

    private final RoomMapper        roomMapper;
    private final SyncMapper        syncMapper;
    private final LectureMapper     lectureMapper;
    private final LectureSyncMapper lectureSyncMapper;

    /**
     * Performs sync of all lectures by fetching original data from StuvAPI
     *
     * @param includeArchived if past lectures should be included
     *
     * @return result of sync action
     *
     * @throws InterruptedException indicates that request was interrupted
     */
    public Optional<SyncDto> updateAll(boolean includeArchived) throws InterruptedException, JsonProcessingException
    {
        try (Session session = this.sessionFactory.openSession())
        {
            Transaction transaction = session.beginTransaction();

            Sync sync = Sync.builder().startedAt(LocalDateTime.now()).build();
            session.persist(sync);
            log.debug("Sync created");

            List<LectureDto> updateLectures = this.stuvApi.fetchLectures(includeArchived);
            log.debug("{} lectures fetched from api", updateLectures.size());

            List<Lecture> persistentLectures = this.getPersistentLectures(session,
                    includeArchived ? LocalDateTime.MIN : sync.getStartedAt());
            log.debug("{} lectures loaded from database", persistentLectures.size());

            Map<String, Room> rooms = this.persistRooms(session,
                    updateLectures.stream().map(LectureDto::getRooms).flatMap(Collection::stream).toList());
            log.debug("{} rooms fetched", rooms.size());

            List<Sync.LectureSync> lectureSyncs = this.syncLectures(session,
                    persistentLectures,
                    updateLectures,
                    rooms::get).stream().map(this.lectureSyncMapper::toEntity).toList();
            log.debug("{} lecture sync details generated", lectureSyncs.size());

            if (lectureSyncs.isEmpty())
            {
                log.debug("No lecture updates found: rolling back");

                transaction.rollback();
                return Optional.empty();
            }

            for (Sync.LectureSync lectureSync : lectureSyncs)
            {
                lectureSync.setSync(sync);
                session.persist(lectureSync);
            }
            log.debug("{} lecture sync details persisted", lectureSyncs.size());

            sync.setData(lectureSyncs);
            sync.setFinishedAt(LocalDateTime.now());
            session.update(sync);

            transaction.commit();
            log.debug("Sync finished");

            return Optional.of(this.syncMapper.toData(sync));
        }
    }

    private Map<String, Room> persistRooms(Session session, Collection<RoomDto> rooms)
    {
        List<Room> persistentRooms = session.createQuery("SELECT item FROM Room item", Room.class).getResultList();
        List<String> persistentRoomNames = persistentRooms.stream().map(Room::getName).toList();

        Set<Room> roomSet = rooms.stream()
                .filter(room -> !persistentRoomNames.contains(room.getName()))
                .map(this.roomMapper::toEntity)
                .collect(Collectors.toSet());

        roomSet.forEach(session::persist);
        roomSet.addAll(persistentRooms);

        return roomSet.stream().collect(Collectors.toMap(Room::getName, room -> room));
    }

    private List<Lecture> getPersistentLectures(Session session, LocalDateTime until)
    {
        return session.createQuery(
                "SELECT DISTINCT item FROM Lecture item LEFT JOIN FETCH item.rooms WHERE item.isArchived IS FALSE AND item.endsAt >= :until",
                Lecture.class).setParameter("until", until).getResultList();
    }

    private List<SyncDto.LectureSync> syncLectures(Session session, List<Lecture> persistentLectures,
            List<LectureDto> updateLectures, Function<String, Room> fetchRoom)
    {
        List<Long> persistentLectureIds = persistentLectures.stream().map(Lecture::getLectureId).toList();
        List<Long> updateLectureIds = updateLectures.stream().map(LectureDto::getLectureId).toList();

        List<SyncDto.LectureSync> syncDetails = new ArrayList<>();
        List<Lecture> mergeLectures = persistentLectures.stream()
                .filter(lecture -> updateLectureIds.contains(lecture.getLectureId()))
                .toList();

        List<Lecture> createdLectures = updateLectures.stream()
                .filter(lecture -> !persistentLectureIds.contains(lecture.getLectureId()))
                .map(this.lectureMapper::toEntity)
                .toList();

        List<Lecture> deletedLectures = persistentLectures.stream()
                .filter(lecture -> !updateLectureIds.contains(lecture.getLectureId()))
                .toList();

        for (Lecture persistentLecture : mergeLectures)
        {
            LectureDto updateLecture = updateLectures.stream()
                    .filter(lecture -> lecture.getLectureId() == persistentLecture.getLectureId())
                    .findAny()
                    .orElseThrow();

            Optional<SyncDto.LectureSync> syncDetail = this.calculateDifference(updateLecture, persistentLecture);
            if (syncDetail.isEmpty())
                continue;

            syncDetails.add(syncDetail.get());

            persistentLecture.setName(updateLecture.getName());
            persistentLecture.setCourse(updateLecture.getCourse().orElse(null));
            persistentLecture.setLecturer(updateLecture.getLecturer().orElse(null));
            persistentLecture.setType(updateLecture.getType().getTypeId());
            persistentLecture.setUpdatedAt(LocalDateTime.now());
            persistentLecture.setStartsAt(updateLecture.getStartsAt());
            persistentLecture.setEndsAt(updateLecture.getEndsAt());
            persistentLecture.setRooms(updateLecture.getRooms()
                    .stream()
                    .map(RoomDto::getName)
                    .map(fetchRoom)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));

            session.update(persistentLecture);
        }

        for (Lecture lecture : createdLectures)
        {
            lecture.setRooms(lecture.getRooms()
                    .stream()
                    .map(Room::getName)
                    .map(fetchRoom)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));

            session.persist(lecture);
            syncDetails.add(this.initialSync(lecture));
        }

        for (Lecture lecture : deletedLectures)
        {
            lecture.setArchived(true);
            session.update(lecture);

            syncDetails.add(this.removalSync(lecture));
        }

        return syncDetails;
    }

    private Optional<SyncDto.LectureSync> calculateDifference(LectureDto lecture, Lecture lectureData)
    {
        List<SyncDto.SyncDetail> details = new ArrayList<>();
        if (!lecture.getName().equals(lectureData.getName()))
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("name")
                    .previousValue(lectureData.getName())
                    .currentValue(lecture.getName())
                    .build());

        if (lecture.getCourse().map(course -> !course.equals(lectureData.getCourse())).orElse(false))
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("course")
                    .previousValue(lectureData.getCourse())
                    .currentValue(lecture.getCourse().orElse(null))
                    .build());

        if (lecture.getLecturer().map(lecturer -> !lecturer.equals(lectureData.getLecturer())).orElse(false))
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("lecturer")
                    .previousValue(lectureData.getLecturer())
                    .currentValue(lecture.getLecturer().orElse(null))
                    .build());

        if (lecture.getType().getTypeId() != lectureData.getType())
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("type")
                    .previousValue(LectureDto.Type.getByTypeId(lectureData.getType()).name())
                    .currentValue(lecture.getType().name())
                    .build());

        if (!lecture.getStartsAt().equals(lectureData.getStartsAt()))
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("starts_at")
                    .previousValue(lectureData.getStartsAt().toString())
                    .currentValue(lecture.getStartsAt().toString())
                    .build());

        if (!lecture.getEndsAt().equals(lectureData.getEndsAt()))
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("ends_at")
                    .previousValue(lectureData.getEndsAt().toString())
                    .currentValue(lecture.getEndsAt().toString())
                    .build());

        Set<String> lectureRoomNames = lecture.getRooms().stream().map(RoomDto::getName).collect(Collectors.toSet());
        Set<String> lectureDataRoomNames = lectureData.getRooms()
                .stream()
                .map(Room::getName)
                .collect(Collectors.toSet());

        if (!lectureRoomNames.equals(lectureDataRoomNames))
            details.add(SyncDto.SyncDetail.builder()
                    .fieldName("rooms")
                    .previousValue(lectureData.getRooms().stream().map(Room::getName).collect(Collectors.joining(", ")))
                    .currentValue(lecture.getRooms().stream().map(RoomDto::getName).collect(Collectors.joining(", ")))
                    .build());

        if (details.isEmpty())
            return Optional.empty();

        return Optional.of(SyncDto.LectureSync.builder()
                .lectureId(lectureData.getId())
                .type(SyncDto.Type.UPDATED)
                .details(details)
                .build());
    }

    private SyncDto.LectureSync initialSync(Lecture lecture)
    {
        return SyncDto.LectureSync.builder()
                .lectureId(lecture.getId())
                .type(SyncDto.Type.CREATED)
                .details(this.getCompleteSyncData(lecture))
                .build();
    }

    private SyncDto.LectureSync removalSync(Lecture lecture)
    {
        return SyncDto.LectureSync.builder()
                .type(SyncDto.Type.DELETED)
                .details(this.getCompleteSyncData(lecture))
                .build();
    }

    private List<SyncDto.SyncDetail> getCompleteSyncData(Lecture lecture)
    {
        return List.of(SyncDto.SyncDetail.builder()
                        .fieldName("lecture_id")
                        .currentValue(Long.toString(lecture.getLectureId()))
                        .build(),
                SyncDto.SyncDetail.builder().fieldName("name").currentValue(lecture.getName()).build(),
                SyncDto.SyncDetail.builder().fieldName("course").currentValue(lecture.getCourse()).build(),
                SyncDto.SyncDetail.builder().fieldName("lecturer").currentValue(lecture.getLecturer()).build(),
                SyncDto.SyncDetail.builder()
                        .fieldName("type")
                        .currentValue(LectureDto.Type.getByTypeId(lecture.getType()).name())
                        .build(),
                SyncDto.SyncDetail.builder()
                        .fieldName("starts_at")
                        .currentValue(lecture.getCreatedAt().toString())
                        .build(),
                SyncDto.SyncDetail.builder()
                        .fieldName("ends_at")
                        .currentValue(lecture.getCreatedAt().toString())
                        .build(),
                SyncDto.SyncDetail.builder()
                        .fieldName("rooms")
                        .currentValue(lecture.getRooms().stream().map(Room::getName).collect(Collectors.joining(", ")))
                        .build());
    }
}
