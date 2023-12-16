package de.cybine.stuvapi.relay.data.action.metadata;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@Table(name = ActionMetadataEntity_.TABLE)
@Entity(name = ActionMetadataEntity_.ENTITY)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionMetadataEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = ActionMetadataEntity_.ID_COLUMN, nullable = false, unique = true)
    private UUID id;

    @Column(name = ActionMetadataEntity_.NAMESPACE_COLUMN, nullable = false)
    private String namespace;

    @Column(name = ActionMetadataEntity_.CATEGORY_COLUMN, nullable = false)
    private String category;

    @Column(name = ActionMetadataEntity_.NAME_COLUMN, nullable = false)
    private String name;

    @Column(name = ActionMetadataEntity_.TYPE_COLUMN, nullable = false)
    private String type;

    @OneToMany(mappedBy = ActionContextEntity_.METADATA_RELATION)
    private Set<ActionContextEntity> contexts;

    public Optional<Set<ActionContextEntity>> getContexts( )
    {
        return Optional.ofNullable(this.contexts);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;

        if (this.getClass() != other.getClass())
            return false;

        WithId<?> that = ((WithId<?>) other);
        if (this.findId().isEmpty() || that.findId().isEmpty())
            return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode( )
    {
        return this.findId().map(Object::hashCode).orElse(0);
    }
}
