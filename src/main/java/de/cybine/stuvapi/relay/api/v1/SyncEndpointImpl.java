package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.api.v1.result.PaginationResult;
import de.cybine.stuvapi.relay.api.v1.result.SyncInfo;
import de.cybine.stuvapi.relay.api.v1.result.SyncSummary;
import de.cybine.stuvapi.relay.config.ApiServerConfig;
import de.cybine.stuvapi.relay.data.sync.SyncDto;
import de.cybine.stuvapi.relay.data.sync.SyncRepository;
import lombok.AllArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class SyncEndpointImpl implements SyncEndpoint
{
    private final ApiServerConfig apiServerConfig;

    private final SyncRepository syncRepository;

    @Override
    public RestResponse<PaginationResult<SyncSummary>> fetchSyncs(int limit, int offset)
    {
        return RestResponse.ok(PaginationResult.<SyncSummary>builder()
                .total((int) this.syncRepository.getSyncCount())
                .limit(limit)
                .offset(offset)
                .next(UriBuilder.fromUri(this.apiServerConfig.baseUrl())
                        .path("/api/v1/sync")
                        .queryParam("limit", limit)
                        .queryParam("offset", offset + limit)
                        .build()
                        .toString())
                .items(this.syncRepository.getSyncs(limit, offset).stream().map(this::toSummary).toList())
                .build());
    }

    @Override
    public RestResponse<PaginationResult<SyncInfo>> fetchSyncInfo(UUID id, String course, boolean detailed, int limit,
            int offset)
    {
        UriBuilder builder = UriBuilder.fromUri(this.apiServerConfig.baseUrl())
                .path(String.format("/api/v1/sync/%s", id))
                .queryParam("limit", limit)
                .queryParam("offset", offset + limit)
                .queryParam("detailed", detailed);

        if (course != null)
            builder.queryParam("course", course);

        return RestResponse.ok(PaginationResult.<SyncInfo>builder()
                .total((int) this.syncRepository.getDetailCount(id, course))
                .limit(limit)
                .offset(offset)
                .next(builder.build().toString())
                .items(this.syncRepository.getDetailsById(id, course, limit, offset)
                        .stream()
                        .map(data -> this.toInfo(data, detailed))
                        .toList())
                .build());
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
