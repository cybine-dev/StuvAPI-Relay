package de.cybine.stuvapi.relay.config;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.data.room.*;
import de.cybine.stuvapi.relay.service.action.data.*;
import de.cybine.stuvapi.relay.service.lecture.*;
import io.quarkus.runtime.*;
import jakarta.annotation.*;
import jakarta.enterprise.context.*;
import lombok.*;

@Startup
@Dependent
@RequiredArgsConstructor
public class ActionDataTypeConfig
{
    private final ActionDataTypeRegistry registry;

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void setup( )
    {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();

        this.registry.registerType("de.cybine.stuvapi.relay:room:v1", typeFactory.constructType(Room.class));
        this.registry.registerType("de.cybine.stuvapi.relay:lecture:v1", typeFactory.constructType(Lecture.class));
        this.registry.registerType("de.cybine.stuvapi.relay:lecture-data:v1",
                typeFactory.constructType(LectureData.class));
        this.registry.registerType("de.cybine.stuvapi.relay:lecture-data-diff:v1",
                typeFactory.constructType(LectureDataDiff.class));
    }
}
