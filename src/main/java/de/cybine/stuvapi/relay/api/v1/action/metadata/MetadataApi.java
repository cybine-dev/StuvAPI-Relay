package de.cybine.stuvapi.relay.api.v1.action.metadata;

import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.api.response.*;
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
@Path("/api/v1/action/metadata")
@Tag(name = "ActionMetadata Resource")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MetadataApi
{
    @GET
    @Path("/find/id/{id}")
    RestResponse<ApiResponse<ActionMetadata>> fetchById(@PathParam("id") UUID id);

    @POST
    RestResponse<ApiResponse<List<ActionMetadata>>> fetch(@Valid @NotNull ApiQuery query);

    @POST
    @Path("find")
    RestResponse<ApiResponse<ActionMetadata>> fetchSingle(@Valid @NotNull ApiQuery query);

    @POST
    @Path("count")
    RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(@Valid @NotNull ApiCountQuery query);

    @POST
    @Path("options")
    RestResponse<ApiResponse<List<Object>>> fetchOptions(@Valid @NotNull ApiOptionQuery query);
}