package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.quarkus.util.api.*;
import de.cybine.quarkus.util.api.query.*;
import de.cybine.quarkus.util.datasource.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

import java.util.*;

import static de.cybine.stuvapi.relay.data.room.RoomEntity_.*;

@Startup
@ApplicationScoped
@RequiredArgsConstructor
public class RoomService
{
    private final GenericApiQueryService<RoomEntity, Room> service = GenericApiQueryService.forType(RoomEntity.class,
            Room.class);

    private final ApiFieldResolver resolver;

    private final Map<String, RoomId> nameCache = new HashMap<>();

    @PostConstruct
    void setup( )
    {
        this.resolver.registerType(Room.class)
                     .withField("id", ID)
                     .withField("name", NAME)
                     .withField("display_name", DISPLAY_NAME)
                     .withRelation("lectures", LECTURES, Lecture.class);

        this.service.fetchEntities(DatasourceQuery.builder().build())
                    .forEach(item -> this.registerRoomId(item.getName(), RoomId.of(item.getId())));
    }

    public List<Room> fetch(ApiQuery query)
    {
        return this.service.fetch(query);
    }

    public Optional<Room> fetchSingle(ApiQuery query)
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

    public void registerRoomId(String name, RoomId id)
    {
        this.nameCache.put(name, id);
    }

    public Optional<RoomId> findIdByName(String name)
    {
        return Optional.ofNullable(this.nameCache.get(name));
    }

    public Set<String> getKnownNames( )
    {
        return this.nameCache.keySet();
    }
}
