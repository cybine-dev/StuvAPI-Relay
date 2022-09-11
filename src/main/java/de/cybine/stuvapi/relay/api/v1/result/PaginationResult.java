package de.cybine.stuvapi.relay.api.v1.result;

import lombok.Data;

import java.util.Collection;
import java.util.Optional;

@Data
public class PaginationResult<T>
{
    private final int total;

    private final int limit;
    private final int offset;

    private final String next;

    private final Collection<T> items;

    public Optional<String> getNext()
    {
        return Optional.ofNullable(this.next);
    }
}
