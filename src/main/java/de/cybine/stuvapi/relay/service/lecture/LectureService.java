package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
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
    private final GenericApiQueryService<LectureEntity, Lecture> service = GenericApiQueryService.forType(
            LectureEntity.class, Lecture.class);

    private final ApiFieldResolver resolver;

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(Lecture.class)
                     .withField("id", ID)
                     .withField("lecture_id", LECTURE_ID)
                     .withField("name", NAME)
                     .withField("course", COURSE)
                     .withField("starts_at", STARTS_AT)
                     .withField("ends_at", ENDS_AT)
                     .withField("type", TYPE)
                     .withField("status", STATUS)
                     .withRelation("rooms", ROOMS, Room.class);
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
