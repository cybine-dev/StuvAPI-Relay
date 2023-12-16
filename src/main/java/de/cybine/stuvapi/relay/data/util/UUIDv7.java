package de.cybine.stuvapi.relay.data.util;

import com.fasterxml.uuid.*;
import lombok.*;

import java.util.*;

@Data
@RequiredArgsConstructor(staticName = "of")
public class UUIDv7
{
    private final UUID id;

    public static UUIDv7 create()
    {
        return new UUIDv7(UUIDv7.generate());
    }

    public static UUID generate()
    {
        return Generators.timeBasedEpochGenerator().generate();
    }
}
