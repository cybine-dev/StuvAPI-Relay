package de.cybine.stuvapi.relay.api.v1.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.service.lecture.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.api.response.*;
import jakarta.annotation.security.*;
import jakarta.enterprise.context.*;
import lombok.*;
import org.jboss.resteasy.reactive.*;

import java.util.*;

@PermitAll
@ApplicationScoped
@AllArgsConstructor
public class LectureResource implements LectureApi
{
    private final LectureService service;

    @Override
    public RestResponse<ApiResponse<List<Lecture>>> fetch(ApiQuery query)
    {
        return ApiResponse.<List<Lecture>>builder().value(this.service.fetch(query)).build().toResponse();
    }

    @Override
    public RestResponse<ApiResponse<Lecture>> fetchSingle(ApiQuery query)
    {
        return ApiResponse.<Lecture>builder()
                          .value(this.service.fetchSingle(query).orElseThrow())
                          .build()
                          .toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(ApiCountQuery query)
    {
        return ApiResponse.<List<ApiCountInfo>>builder().value(this.service.fetchTotal(query)).build().toResponse();
    }

    @Override
    public RestResponse<ApiResponse<List<Object>>> fetchOptions(ApiOptionQuery query)
    {
        return ApiResponse.<List<Object>>builder().value(this.service.fetchOptions(query)).build().toResponse();
    }
}
