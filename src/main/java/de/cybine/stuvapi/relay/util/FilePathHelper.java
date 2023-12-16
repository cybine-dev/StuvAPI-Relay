package de.cybine.stuvapi.relay.util;

import lombok.experimental.*;

import java.net.*;
import java.nio.file.*;
import java.util.*;

@UtilityClass
public class FilePathHelper
{
    public static Path resolvePath(String path) throws URISyntaxException
    {
        if (path.startsWith("%resources%/"))
            return Path.of(Objects.requireNonNull(
                    FilePathHelper.class.getClassLoader().getResource(path.replace("%resources%/", ""))).toURI());

        return Path.of(path);
    }
}
