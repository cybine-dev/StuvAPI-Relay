package de.cybine.stuvapi.relay.util;

import lombok.experimental.*;
import lombok.extern.log4j.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

@Log4j2
@UtilityClass
public class FilePathHelper
{
    public static Optional<Path> resolvePath(String path) throws URISyntaxException
    {
        if (path.startsWith("%resources%/"))
        {
            URL resourceUrl = FilePathHelper.class.getClassLoader().getResource(path.replace("%resources%/", ""));
            if (resourceUrl == null)
            {
                log.warn("Cloud not find resource-path '{}'. Please consider configuring custom a path.", path);
                return Optional.empty();
            }

            return Optional.of(Path.of(resourceUrl.toURI()));
        }

        return Optional.of(Path.of(path));
    }

    public static Optional<String> tryRead(Path path)
    {
        try
        {
            return Optional.of(Files.readString(path));
        }
        catch (IOException e)
        {
            return Optional.empty();
        }
    }
}
