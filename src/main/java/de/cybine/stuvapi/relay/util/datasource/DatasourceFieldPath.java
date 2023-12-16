package de.cybine.stuvapi.relay.util.datasource;

import jakarta.persistence.criteria.*;
import lombok.*;

import java.util.*;
import java.util.stream.*;

@Data
public class DatasourceFieldPath
{
    private final List<DatasourceField> path;

    @Builder(builderClassName = "Generator")
    public DatasourceFieldPath(@Singular("field") List<DatasourceField> path)
    {
        if(path.isEmpty())
        {
            throw new IllegalArgumentException("Path cannot be emtpy");
        }

        this.path = path;
    }

    public DatasourceField getLast()
    {
        return this.path.get(this.getLength() - 1);
    }

    public int getLength()
    {
        return this.path.size();
    }

    public String asString()
    {
        return this.path.stream().map(DatasourceField::getName).collect(Collectors.joining("."));
    }

    @SuppressWarnings("unchecked")
    public static <T> Path<T> resolvePath(Path<?> parent, String path)
    {
        Path<?> field = parent;
        for(String fieldPath : path.split("\\."))
        {
            field = field.get(fieldPath);
        }

        return (Path<T>) field;
    }
}
