package de.cybine.stuvapi.relay.config;

import de.cybine.quarkus.util.converter.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.lecture.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

@Startup
@Dependent
@RequiredArgsConstructor
public class ConverterConfig
{
    private final ConverterRegistry registry;

    private final RoomService roomService;

    @PostConstruct
    public void setup( )
    {
        this.registry.addEntityMapper(new ActionContextMapper());
        this.registry.addEntityMapper(new ActionProcessMapper());

        this.registry.addEntityMapper(new LectureMapper());
        this.registry.addEntityMapper(new RoomMapper());
        this.registry.addEntityMapper(new LectureDataMapper(this.roomService));
    }
}
