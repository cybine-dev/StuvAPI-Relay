package de.cybine.stuvapi.relay.api.v1.action.handle;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.util.api.response.*;
import io.quarkus.security.*;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.parameters.*;
import org.eclipse.microprofile.openapi.annotations.tags.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@Authenticated
@Path("/api/v1/action/handle")
@Tag(name = "ActionHandle Resource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HandleApi
{
    @POST
    @Path("/create/{namespace}/{category}/{name}")
    @Parameter(name = "namespace", required = true, description = "Namespace of the action-context")
    @Parameter(name = "category", required = true, description = "Category of the action-context")
    @Parameter(name = "name", required = true, description = "Name of the action-context")
    @Parameter(name = "item-id", description = "ID of the item to apply the action to")
    RestResponse<ApiResponse<ActionContext>> create(@PathParam("namespace") String namespace,
            @PathParam("category") String category, @PathParam("name") String name,
            @QueryParam("item-id") String itemId);

    @POST
    @Path("/terminate")
    @Parameter(name = "correlation-id",
               required = true,
               description = "Correlation-ID of the action-context to terminate")
    RestResponse<ApiResponse<Void>> terminate(@QueryParam("correlation-id") @NotNull String correlationId);

    @POST
    @Path("/process")
    @Parameter(name = "correlation-id",
               required = true,
               description = "Correlation-ID of the action-context to process")
    @Parameter(name = "event-id",
               description =
                       "Event-ID to optionally check for current state to avoid processing with out-of-date " +
                               "information")
    @Parameter(name = "action", required = true, description = "Action to perform (next-state of the context)")
    @Parameter(name = "data", description = "Data to add to the process")
    RestResponse<ApiResponse<ActionProcess>> process(@QueryParam("correlation-id") @NotNull String correlationId,
            @QueryParam("event-id") String eventId, @QueryParam("action") @NotNull String action,
            Map<String, Object> data);

    @GET
    @Path("/available-actions/{correlation-id}")
    @Parameter(name = "correlation-id",
               required = true,
               description = "Correlation-ID of the action-context to process")
    RestResponse<ApiResponse<List<String>>> fetchAvailableActions(
            @PathParam("correlation-id") @NotNull String correlationId);
}
