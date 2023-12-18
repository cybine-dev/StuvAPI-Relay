package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.util.api.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.datasource.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

import java.util.*;

import static de.cybine.stuvapi.relay.data.lecture.LectureEntity_.*;

@Startup
@ApplicationScoped
@RequiredArgsConstructor
public class LectureService
{
    private final GenericDatasourceService<LectureEntity, Lecture> service = GenericDatasourceService.forType(
            LectureEntity.class, Lecture.class);

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerTypeRepresentation(Lecture.class, LectureEntity.class)
                     .registerField("id", ID)
                     .registerField("lecture_id", LECTURE_ID)
                     .registerField("name", NAME)
                     .registerField("course", COURSE)
                     .registerField("starts_at", STARTS_AT)
                     .registerField("ends_at", ENDS_AT)
                     .registerField("type", TYPE)
                     .registerField("status", STATUS)
                     .registerField("rooms", ROOMS);
    }

    public List<Lecture> fetch(ApiQuery query)
    {
        return this.service.fetch(query);
    }

    public Optional<Lecture> fetchSingle(ApiQuery query)
    {
        return this.service.fetchSingle(query);
    }

    public <O> List<O> fetchOptions(ApiOptionQuery query)
    {
        return this.service.fetchOptions(query);
    }

    public List<ApiCountInfo> fetchTotal(ApiCountQuery query)
    {
        return this.service.fetchTotal(query);
    }
}
