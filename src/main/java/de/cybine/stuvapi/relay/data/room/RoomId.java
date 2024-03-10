package de.cybine.stuvapi.relay.data.room;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.quarkus.data.util.*;
import de.cybine.quarkus.data.util.primitive.*;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.io.*;
import java.util.*;

@Data
@RequiredArgsConstructor(staticName = "of")
@Schema(type = SchemaType.STRING, implementation = UUID.class)
public class RoomId implements Id<UUID>
{
    @JsonValue
    @Schema(hidden = true)
    private final UUID value;

    public static RoomId create( )
    {
        return RoomId.of(UUIDv7.generate());
    }

    public static class Deserializer extends JsonDeserializer<RoomId>
    {
        @Override
        public RoomId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String value = p.nextTextValue();
            if(value == null)
                return null;

            return RoomId.of(UUID.fromString(value));
        }
    }
}
