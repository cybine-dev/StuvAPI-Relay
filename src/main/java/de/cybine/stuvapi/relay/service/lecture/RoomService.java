package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.util.api.*;
import de.cybine.stuvapi.relay.util.api.query.*;
import de.cybine.stuvapi.relay.util.datasource.*;
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
    private final GenericDatasourceService<RoomEntity, Room> service = GenericDatasourceService.forType(
            RoomEntity.class, Room.class);

    private final ApiFieldResolver resolver;

    private final Map<String, RoomId> nameCache = new HashMap<>();

    @PostConstruct
    void setup( )
    {
        this.resolver.registerTypeRepresentation(Room.class, RoomEntity.class)
                     .registerField("id", ID)
                     .registerField("name", NAME)
                     .registerField("display_name", DISPLAY_NAME)
                     .registerField("lectures", LECTURES);

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

    public Set<String> getKnownNames()
    {
        return this.nameCache.keySet();
    }
}
