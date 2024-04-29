package de.cybine.stuvapi.relay.api.v1.action.process;

import de.cybine.quarkus.api.response.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.cloudevent.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import jakarta.annotation.security.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.tags.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@PermitAll
@Path("/api/v1/action/process")
@Tag(name = "ActionProcess Resource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProcessApi
{
    @GET
    @Path("/find/id/{id}")
    RestResponse<ApiResponse<ActionProcess>> fetchById(@PathParam("id") UUID id);

    @GET
    @Path("/find/event-id/{event-id}")
    RestResponse<ApiResponse<ActionProcess>> fetchByEventId(@PathParam("event-id") String eventId);

    @GET
    @Path("/find/correlation-id/{correlation-id}")
    RestResponse<ApiResponse<List<ActionProcess>>> fetchByCorrelationId(
            @PathParam("correlation-id") String correlationId);

    @GET
    @Path("/cloud-event/event-id/{event-id}")
    RestResponse<ApiResponse<CloudEvent>> fetchCloudEventByEventId(@PathParam("event-id") String eventId);

    @GET
    @Path("/cloud-event/correlation-id/{correlation-id}")
    RestResponse<ApiResponse<List<CloudEvent>>> fetchCloudEventsByCorrelationId(
            @PathParam("correlation-id") String correlationId);

    @POST
    RestResponse<ApiResponse<List<ActionProcess>>> fetch(@Valid @NotNull ApiQuery query);

    @POST
    @Path("find")
    RestResponse<ApiResponse<ActionProcess>> fetchSingle(@Valid @NotNull ApiQuery query);

    @POST
    @Path("count")
    RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(@Valid @NotNull ApiCountQuery query);

    @POST
    @Path("options")
    RestResponse<ApiResponse<List<Object>>> fetchOptions(@Valid @NotNull ApiOptionQuery query);
}
