package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.service.calendar.CalendarService;
import lombok.AllArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;

@ApplicationScoped
@AllArgsConstructor
public class CalendarEndpointImpl implements CalendarEndpoint
{
    private final CalendarService calendarService;

    @Override
    public RestResponse<File> fetchCalendar(String course)
    {
        File calendarFile = this.calendarService.getCalendarFile(course);
        if(!calendarFile.exists())
            return RestResponse.notFound();

        return RestResponse.ok(calendarFile);
    }
}
