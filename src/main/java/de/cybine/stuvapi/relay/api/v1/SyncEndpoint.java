package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.api.v1.result.PaginationResult;
import de.cybine.stuvapi.relay.api.v1.result.SyncInfo;
import de.cybine.stuvapi.relay.api.v1.result.SyncSummary;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("api/v1/sync")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Sync Resource", description = "Provides detailed information about lecture changes")
public interface SyncEndpoint
{
    @GET
    RestResponse<PaginationResult<SyncSummary>> fetchSyncs(
            @QueryParam("limit") @Min(1) @Max(50) @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset);

    @GET
    @Path("/{id}")
    RestResponse<PaginationResult<SyncInfo>> fetchSyncInfo(UUID id, @QueryParam("course") String course,
            @QueryParam("detailed") @DefaultValue("true") boolean detailed,
            @QueryParam("limit") @Min(1) @Max(50) @DefaultValue("20") int limit,
            @QueryParam("offset") @DefaultValue("0") int offset);
}
