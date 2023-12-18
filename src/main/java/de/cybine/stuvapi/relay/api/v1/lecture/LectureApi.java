package de.cybine.stuvapi.relay.api.v1.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
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
@Path("api/v1/lecture")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Lecture", description = "Provides information about lectures")
public interface LectureApi
{
    @POST
    RestResponse<ApiResponse<List<Lecture>>> fetch(@Valid @NotNull ApiQuery query);

    @POST
    @Path("find")
    RestResponse<ApiResponse<Lecture>> fetchSingle(@Valid @NotNull ApiQuery query);

    @POST
    @Path("count")
    RestResponse<ApiResponse<List<ApiCountInfo>>> fetchCount(@Valid @NotNull ApiCountQuery query);

    @POST
    @Path("options")
    RestResponse<ApiResponse<List<Object>>> fetchOptions(@Valid @NotNull ApiOptionQuery query);
}
