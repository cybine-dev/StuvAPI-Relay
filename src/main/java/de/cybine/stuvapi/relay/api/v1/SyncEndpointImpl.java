package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.api.v1.result.PaginationResult;
import de.cybine.stuvapi.relay.api.v1.result.SyncInfo;
import de.cybine.stuvapi.relay.api.v1.result.SyncSummary;
import de.cybine.stuvapi.relay.data.sync.SyncDto;
import de.cybine.stuvapi.relay.data.sync.SyncRepository;
import lombok.AllArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class SyncEndpointImpl implements SyncEndpoint
{
    private final SyncRepository syncRepository;

    @Override
    public RestResponse<PaginationResult<SyncSummary>> fetchSyncs(UriInfo uriInfo, int limit, int offset)
    {
        return RestResponse.ok(new PaginationResult<>(((Long) this.syncRepository.getSyncCount()).intValue(),
                limit,
                offset,
                offset + limit >= offset ? null : uriInfo.getRequestUriBuilder()
                        .replaceQueryParam("offset", offset + limit)
                        .build()
                        .toString(),
                this.syncRepository.getSyncs(limit, offset).stream().map(this::toSummary).toList()));
    }

    @Override
    public RestResponse<PaginationResult<SyncInfo>> fetchSyncInfo(UriInfo uriInfo, UUID id, boolean detailed, int limit,
            int offset)
    {
        return RestResponse.ok(new PaginationResult<>(((Long) this.syncRepository.getDetailCount(id)).intValue(),
                limit,
                offset,
                offset + limit >= offset ? null : uriInfo.getRequestUriBuilder()
                        .replaceQueryParam("offset", offset + limit)
                        .build()
                        .toString(),
                this.syncRepository.getDetailsById(id, limit, offset)
                        .stream()
                        .map(data -> this.toInfo(data, detailed))
                        .toList()));
    }

    private SyncSummary toSummary(SyncDto data)
    {
        return new SyncSummary(data.getId().orElseThrow(), data.getStartedAt(), data.getFinishedAt());
    }

    private SyncInfo toInfo(SyncDto.LectureSync data, boolean detailed)
    {
        return new SyncInfo(data.getId().orElseThrow(),
                data.getSyncId().orElseThrow(),
                data.getType(),
                data.getLecture().orElseThrow(),
                detailed ? data.getDetails() : Collections.emptyList());
    }
}
