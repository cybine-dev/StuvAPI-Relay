package de.cybine.stuvapi.relay.api.v1.calendar;

import de.cybine.stuvapi.relay.service.calendar.*;
import jakarta.enterprise.context.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.io.*;

@ApplicationScoped
@RequiredArgsConstructor
public class CalendarResource implements CalendarApi
{
    private final CalendarService calendarService;

    @Override
    public RestResponse<String> fetchCalendar(String course) throws IOException
    {
        try
        {
            return RestResponse.ok(this.calendarService.getCalendarFileContent(course));
        }
        catch (FileNotFoundException exception)
        {
            return RestResponse.notFound();
        }
    }
}
