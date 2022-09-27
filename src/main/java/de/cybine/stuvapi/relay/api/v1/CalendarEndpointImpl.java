package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.service.calendar.CalendarService;
import lombok.AllArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileNotFoundException;
import java.io.IOException;

@ApplicationScoped
@AllArgsConstructor
public class CalendarEndpointImpl implements CalendarEndpoint
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
