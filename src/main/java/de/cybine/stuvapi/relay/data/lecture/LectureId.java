package de.cybine.stuvapi.relay.data.lecture;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.data.util.*;
import de.cybine.stuvapi.relay.data.util.primitive.*;
import lombok.*;
import lombok.extern.jackson.*;
import org.eclipse.microprofile.openapi.annotations.enums.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.io.*;
import java.util.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@RequiredArgsConstructor(staticName = "of")
@Schema(type = SchemaType.STRING, implementation = UUID.class)
public class LectureId implements Id<UUID>
{
    @JsonValue
    @Schema(hidden = true)
    private final UUID value;

    public static LectureId create( )
    {
        return LectureId.of(UUIDv7.generate());
    }

    public static class Deserializer extends JsonDeserializer<LectureId>
    {
        @Override
        public LectureId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String value = p.nextTextValue();
            if(value == null)
                return null;

            return LectureId.of(UUID.fromString(value));
        }
    }
}
