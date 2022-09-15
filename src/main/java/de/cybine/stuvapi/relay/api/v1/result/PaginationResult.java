package de.cybine.stuvapi.relay.api.v1.result;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Optional;

@Data
@Builder(builderClassName = "Builder")
public class PaginationResult<T>
{
    private final int total;

    private final int limit;
    private final int offset;

    private final String next;

    private final Collection<T> items;

    public Optional<String> getNext()
    {
        if(this.total <= this.limit + this.offset)
            return Optional.empty();

        return Optional.of(this.next);
    }
}
