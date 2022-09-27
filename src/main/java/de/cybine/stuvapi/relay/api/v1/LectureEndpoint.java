package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Path("api/v1/lecture")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Lecture", description = "Provides information about lectures")
public interface LectureEndpoint
{
    @GET
    Collection<LectureDto> fetchAll(@RestQuery LocalDateTime from, @RestQuery LocalDateTime until,
            @RestQuery String course);

    @GET
    @Path("/{id}")
    LectureDto fetch(UUID id);
}
