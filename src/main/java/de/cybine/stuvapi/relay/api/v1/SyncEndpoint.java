package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.data.sync.SyncDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/sync")
@Produces(MediaType.APPLICATION_JSON)
public interface SyncEndpoint
{
    @GET
    @Path("/all")
    Collection<SyncDto> fetchAll( );
}
