package de.cybine.stuvapi.relay.api.v1;

import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;

@Path("api/v1/calendar")
public interface CalendarEndpoint
{
    @GET
    RestResponse<File> fetchCalendar(@RestQuery String course);
}
