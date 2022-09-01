package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.data.sync.SyncDto;
import de.cybine.stuvapi.relay.data.sync.SyncRepository;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;

@ApplicationScoped
@AllArgsConstructor
public class SyncEndpointImpl implements SyncEndpoint
{
    private final SyncRepository syncRepository;

    @Override
    public Collection<SyncDto> fetchAll( )
    {
        return this.syncRepository.getAll();
    }
}
