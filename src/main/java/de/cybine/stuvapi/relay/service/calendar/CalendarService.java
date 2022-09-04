package de.cybine.stuvapi.relay.service.calendar;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneAssignment;
import biweekly.property.Classification;
import biweekly.property.Method;
import biweekly.util.Duration;
import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import de.cybine.stuvapi.relay.data.lecture.LectureRepository;
import de.cybine.stuvapi.relay.data.room.RoomDto;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@ApplicationScoped
@AllArgsConstructor
public class CalendarService
{
    private final LectureRepository lectureRepository;

    public File getCalendarFile(String course)
    {
        return new File(String.format("calendar/%s.ics", course.toLowerCase()));
    }

    public void replaceCalendarFiles( ) throws IOException
    {
        Path folderPath = Path.of("calendar");
        if (!Files.exists(folderPath))
        {
            Files.createDirectory(folderPath);
            log.info("Folder for iCalendar files created. Path: {}", folderPath.toAbsolutePath().toString());
        }

        Map<String, List<LectureDto>> courseLectures = new HashMap<>();
        for (LectureDto data : this.lectureRepository.getAllLectures())
            courseLectures.computeIfAbsent(data.getCourse().orElse("common"), id -> new ArrayList<>()).add(data);

        log.info("Updating {} iCalendar files", courseLectures.size());
        for (Map.Entry<String, List<LectureDto>> entry : courseLectures.entrySet())
        {
            Path path = Path.of(folderPath.toString(), String.format("%s.ics", entry.getKey().toLowerCase()));
            if (!Files.exists(path))
                Files.createFile(path);

            Files.writeString(path, this.getCalendar(entry.getKey(), entry.getValue()).write());

            log.debug("Updated iCalendar file for course {}", entry.getKey());
        }
    }

    private ICalendar getCalendar(String course, List<LectureDto> lectures)
    {
        ICalendar calendar = new ICalendar();
        calendar.getTimezoneInfo().setDefaultTimezone(TimezoneAssignment.download(TimeZone.getDefault(), true));
        calendar.setRefreshInterval(Duration.builder().minutes(30).build());
        calendar.setVersion(ICalVersion.V2_0);
        calendar.setMethod(Method.publish());
        calendar.setName(course);
        calendar.setDescription(String.format("Vorlesungsplan %s", course));

        String version = this.getClass().getPackage().getImplementationVersion();
        calendar.setProductId(String.format("-//Cybine//StuvAPI-Relay v%s//DE",
                version == null ? "DEVELOPMENT" : version));

        lectures.stream().map(this::getEvent).forEach(calendar::addEvent);

        return calendar;
    }

    private VEvent getEvent(LectureDto data)
    {
        VEvent event = new VEvent();
        event.setUid(String.format("%s@dhbw.cybine.de", data.getId().orElseThrow()));
        event.setClassification(Classification.public_());

        event.setSummary(data.getName());
        event.setOrganizer(data.getLecturer().orElse("N/A"));
        event.setLocation(data.getRooms().stream().map(RoomDto::getDisplayName).collect(Collectors.joining("; ")));

        event.setCreated(this.transformLocalDateTime(data.getCreatedAt()));
        event.setLastModified(this.transformLocalDateTime(data.getUpdatedAt()));
        event.setDateStart(this.transformLocalDateTime(data.getStartsAt()), true);
        event.setDateEnd(this.transformLocalDateTime(data.getEndsAt()), true);

        if (data.isExam())
            event.addCategories(CalendarCategories.EXAM.getDisplayName());

        if (data.isHoliday())
            event.addCategories(CalendarCategories.HOLIDAY.getDisplayName());

        if (data.isRegularLecture())
        {
            event.addCategories(CalendarCategories.LECTURE.getDisplayName());
            switch (data.getType())
            {
                case ONLINE -> event.addCategories(CalendarCategories.ONLINE.getDisplayName());
                case PRESENCE -> event.addCategories(CalendarCategories.PRESENCE.getDisplayName());
                case HYBRID -> event.addCategories(CalendarCategories.HYBRID.getDisplayName());
            }
        }

        return event;
    }

    private Date transformLocalDateTime(LocalDateTime dateTime)
    {
        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }
}
