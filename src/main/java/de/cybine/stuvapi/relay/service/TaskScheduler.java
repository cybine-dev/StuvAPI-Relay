package de.cybine.stuvapi.relay.service;

import com.fasterxml.jackson.core.*;
import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.service.calendar.*;
import de.cybine.stuvapi.relay.service.stuv.*;
import io.quarkus.scheduler.*;
import jakarta.enterprise.context.*;
import lombok.*;
import lombok.extern.log4j.*;

import java.io.*;

@Log4j2
@ApplicationScoped
@AllArgsConstructor
public class TaskScheduler
{
    private final ConverterConfig converterConfig;

    private final StuvApiService  stuvApiService;
    private final CalendarService calendarService;

    @Scheduled(every = "30m", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void syncAllLectures( ) throws InterruptedException, JsonProcessingException
    {
        this.stuvApiService.updateAll();
    }

    @Scheduled(every = "30m", delay = 5, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void syncCalendarFiles( ) throws IOException
    {
        this.calendarService.replaceCalendarFiles();
    }
}
