package de.cybine.stuvapi.relay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.cybine.stuvapi.relay.data.sync.SyncDto;
import de.cybine.stuvapi.relay.service.stuv.StuvApiService;
import io.quarkus.scheduler.Scheduled;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;

@Log4j2
@ApplicationScoped
@AllArgsConstructor
public class TaskScheduler
{
    private final StuvApiService stuvApiService;

    @Scheduled(every = "12h", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void syncAllLectures( ) throws InterruptedException, JsonProcessingException
    {
        this.logLectureSyncResult(this.stuvApiService.updateAll(true)
                .map(SyncDto::getData)
                .orElse(Collections.emptyList()));
    }

    @Scheduled(every = "10m", delay = 5, concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void syncFutureLectures( ) throws InterruptedException, JsonProcessingException
    {
        this.logLectureSyncResult(this.stuvApiService.updateAll(false)
                .map(SyncDto::getData)
                .orElse(Collections.emptyList()));
    }

    private void logLectureSyncResult(final List<SyncDto.LectureSync> syncData)
    {
        log.info("{} lectures updated: {} created | {} patched | {} removed",
                syncData.size(),
                syncData.stream().filter(update -> update.getType() == SyncDto.Type.CREATED).count(),
                syncData.stream().filter(update -> update.getType() == SyncDto.Type.UPDATED).count(),
                syncData.stream().filter(update -> update.getType() == SyncDto.Type.DELETED).count());
    }
}
