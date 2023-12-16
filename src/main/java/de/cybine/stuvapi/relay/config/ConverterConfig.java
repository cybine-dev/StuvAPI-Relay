package de.cybine.stuvapi.relay.config;

import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.lecture.*;
import de.cybine.stuvapi.relay.util.api.converter.*;
import de.cybine.stuvapi.relay.util.cloudevent.*;
import de.cybine.stuvapi.relay.util.converter.*;
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

    private final ObjectMapper      objectMapper;
    private final ApplicationConfig applicationConfig;

    private final RoomService roomService;

    @PostConstruct
    public void setup( )
    {
        this.registry.addConverter(new ApiConditionDetailConverter(this.objectMapper));
        this.registry.addConverter(new ApiConditionInfoConverter());
        this.registry.addConverter(new ApiCountQueryConverter());
        this.registry.addConverter(new ApiCountRelationConverter());
        this.registry.addConverter(new ApiQueryConverter());
        this.registry.addConverter(new ApiOptionQueryConverter());
        this.registry.addConverter(new ApiOrderInfoConverter());
        this.registry.addConverter(new ApiOptionQueryConverter());
        this.registry.addConverter(new ApiPaginationInfoConverter());
        this.registry.addConverter(new ApiRelationInfoConverter(this.applicationConfig));
        this.registry.addEntityMapper(new CountInfoMapper());

        this.registry.addEntityMapper(new ActionContextMapper());
        this.registry.addEntityMapper(new ActionMetadataMapper());
        this.registry.addEntityMapper(new ActionProcessMapper());
        this.registry.addConverter(new CloudEventConverter(this.applicationConfig));

        this.registry.addEntityMapper(new LectureMapper());
        this.registry.addEntityMapper(new RoomMapper());
        this.registry.addEntityMapper(new LectureDataMapper(this.roomService));
    }
}
