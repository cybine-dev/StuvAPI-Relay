package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.data.sync.SyncDto;
import org.jboss.resteasy.reactive.RestResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.UUID;

@Path("api/v1/sync")
@Produces(MediaType.APPLICATION_JSON)
public interface SyncEndpoint
{
    @GET
    @Path("/all")
    RestResponse<Collection<SyncDto>> fetchAll( );

    @GET
    @Path("/ids")
    RestResponse<Collection<UUID>> fetchSyncIds( );

    @GET
    @Path("/{id}")
    RestResponse<SyncDto> fetchSync(UUID id);
}
