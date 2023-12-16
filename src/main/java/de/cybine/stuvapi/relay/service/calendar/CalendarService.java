package de.cybine.stuvapi.relay.service.calendar;

import biweekly.*;
import biweekly.component.*;
import biweekly.io.*;
import biweekly.property.*;
import biweekly.util.Duration;
import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.data.util.primitive.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import jakarta.ejb.*;
import jakarta.enterprise.context.*;
import lombok.*;
import lombok.extern.log4j.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Log4j2
@Startup
@ApplicationScoped
@AllArgsConstructor
public class CalendarService
{
    private final ApplicationConfig applicationConfig;

    private final GenericDatasourceService<LectureEntity, Lecture> lectureService = GenericDatasourceService.forType(
            LectureEntity.class, Lecture.class);

    public String getCalendarFileContent(String course) throws IOException
    {
        Path path = Path.of(String.format("calendar/%s.ics", course.toLowerCase().replace("/", "-")));
        if (!Files.exists(path))
            throw new FileNotFoundException(path.toString());

        return Files.readString(path);
    }

    public void replaceCalendarFiles( ) throws IOException
    {
        Path folderPath = Path.of("calendar");
        if (!Files.exists(folderPath))
        {
            Files.createDirectory(folderPath);
            log.info("Folder for iCalendar files created. Path: {}", folderPath.toAbsolutePath().toString());
        }

        DatasourceQuery query = DatasourceQuery.builder()
                                               .relation(DatasourceHelper.fetch(LectureEntity_.ROOMS))
                                               .build();

        List<Lecture> lectures = this.lectureService.fetch(query);
        Map<String, List<Lecture>> courseLectures = new HashMap<>();
        for (Lecture data : lectures)
            courseLectures.computeIfAbsent(data.getCourse().orElse("common").replace("/", "-"), id -> new ArrayList<>())
                          .add(data);

        log.info("Updating {} iCalendar files", courseLectures.size());
        for (Map.Entry<String, List<Lecture>> entry : courseLectures.entrySet())
        {
            Path path = Path.of(folderPath.toString(), String.format("%s.ics", entry.getKey().toLowerCase()));
            if (!Files.exists(path))
                Files.createFile(path);

            Files.writeString(path, this.getCalendar(entry.getKey(), entry.getValue()).write());

            log.debug("Updated iCalendar file for course {}", entry.getKey());
        }
    }

    private ICalendar getCalendar(String course, List<Lecture> lectures)
    {
        ICalendar calendar = new ICalendar();
        calendar.getTimezoneInfo().setDefaultTimezone(TimezoneAssignment.download(TimeZone.getDefault(), true));
        calendar.setRefreshInterval(Duration.builder().minutes(30).build());
        calendar.setVersion(ICalVersion.V2_0);
        calendar.setMethod(Method.publish());
        calendar.setName(course);
        calendar.setDescription(String.format("Vorlesungsplan %s", course));
        calendar.setLastModified(this.transformLocalDateTime(ZonedDateTime.now()));

        String version = this.getClass().getPackage().getImplementationVersion();
        calendar.setProductId(
                String.format("-//Cybine//StuvAPI-Relay v%s//DE", version == null ? "DEVELOPMENT" : version));

        lectures.stream().map(this::getEvent).forEach(calendar::addEvent);

        return calendar;
    }

    private VEvent getEvent(Lecture data)
    {
        VEvent event = new VEvent();
        event.setUid(String.format("%s@stuvapi-relay.cybine.de", data.findId().map(Id::getValue).orElseThrow()));
        event.setClassification(Classification.public_());

        event.setOrganizer(new Organizer(this.applicationConfig.serviceName(), this.applicationConfig.email()));
        event.setSummary(data.getName());
        event.addCategories(this.getEventCategories(data));
        event.setLocation(data.getRooms()
                              .orElse(Collections.emptySet())
                              .stream()
                              .map(Room::getDisplayName)
                              .collect(Collectors.joining("; ")));

        event.setDateStart(this.transformLocalDateTime(data.getStartsAt()), true);
        event.setDateEnd(this.transformLocalDateTime(data.getEndsAt()), true);

        return event;
    }

    private List<String> getEventCategories(Lecture data)
    {
        List<String> categories = new ArrayList<>();
        if (data.isExam())
            categories.add(CalendarCategories.EXAM.getDisplayName());

        if (data.isHoliday())
            categories.add(CalendarCategories.HOLIDAY.getDisplayName());

        if (data.isRegularLecture())
        {
            categories.add(data.getName());
            categories.add(CalendarCategories.LECTURE.getDisplayName());
            switch (data.getType())
            {
                case ONLINE -> categories.add(CalendarCategories.ONLINE.getDisplayName());
                case PRESENCE -> categories.add(CalendarCategories.PRESENCE.getDisplayName());
                case HYBRID -> categories.add(CalendarCategories.HYBRID.getDisplayName());
            }
        }

        return categories;
    }

    private Date transformLocalDateTime(ZonedDateTime dateTime)
    {
        return Date.from(dateTime.toInstant());
    }
}
