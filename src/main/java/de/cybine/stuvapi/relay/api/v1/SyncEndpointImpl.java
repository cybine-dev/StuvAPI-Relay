package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.data.sync.SyncDto;
import de.cybine.stuvapi.relay.data.sync.SyncRepository;
import lombok.AllArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class SyncEndpointImpl implements SyncEndpoint
{
    private final SyncRepository syncRepository;

    @Override
    public RestResponse<Collection<SyncDto>> fetchAll()
    {
        return RestResponse.ok(this.syncRepository.getAll());
    }

    @Override
    public RestResponse<Collection<UUID>> fetchSyncIds()
    {
        return RestResponse.ok(this.syncRepository.getAllIds());
    }

    @Override
    public RestResponse<SyncDto> fetchSync(UUID id)
    {
        return this.syncRepository.getById(id).map(RestResponse::ok).orElse(RestResponse.notFound());
    }
}
