package de.cybine.stuvapi.relay.api.v1;

import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;

@Path("api/v1/calendar")
@Produces(MediaType.TEXT_PLAIN)
public interface CalendarEndpoint
{
    @GET
    RestResponse<File> fetchCalendar(@RestQuery @NotNull String course);
}
