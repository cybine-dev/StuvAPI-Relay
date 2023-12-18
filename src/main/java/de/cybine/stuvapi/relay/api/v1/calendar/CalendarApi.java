package de.cybine.stuvapi.relay.api.v1.calendar;

import jakarta.annotation.security.*;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.tags.*;
import org.jboss.resteasy.reactive.*;

import java.io.*;

@PermitAll
@Path("/api/v1/calendar")
@Tag(name = "Calendar Resource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CalendarApi
{
    @GET
    @Produces("text/calendar")
    RestResponse<String> fetchCalendar(@RestQuery @NotNull String course) throws IOException;
}
