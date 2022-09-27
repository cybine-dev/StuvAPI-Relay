package de.cybine.stuvapi.relay.api.v1;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;

@Path("api/v1/calendar")
@Tag(name = "Calendar", description = "Provides calendar ready lecture information")
public interface CalendarEndpoint
{
    @GET
    @Produces("text/calendar")
    RestResponse<String> fetchCalendar(@RestQuery @NotNull String course) throws IOException;
}