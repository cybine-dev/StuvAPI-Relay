package de.cybine.stuvapi.relay.data.action.metadata;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.data.util.*;
import de.cybine.stuvapi.relay.data.util.primitive.*;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.enums.*;
import org.eclipse.microprofile.openapi.annotations.media.*;

import java.io.*;
import java.util.*;

@Data
@RequiredArgsConstructor(staticName = "of")
@Schema(type = SchemaType.STRING, implementation = UUID.class)
public class ActionMetadataId implements Serializable, Id<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonValue
    @Schema(hidden = true)
    private final UUID value;

    public static ActionMetadataId create( )
    {
        return ActionMetadataId.of(UUIDv7.generate());
    }

    public static class Deserializer extends JsonDeserializer<ActionMetadataId>
    {
        @Override
        public ActionMetadataId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String value = p.nextTextValue();
            if(value == null)
                return null;

            return ActionMetadataId.of(UUID.fromString(value));
        }
    }
}
