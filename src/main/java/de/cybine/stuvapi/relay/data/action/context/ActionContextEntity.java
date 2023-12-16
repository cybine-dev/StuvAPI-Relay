package de.cybine.stuvapi.relay.data.action.context;

import de.cybine.stuvapi.relay.data.action.metadata.*;
import de.cybine.stuvapi.relay.data.action.process.*;
import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@Table(name = ActionContextEntity_.TABLE)
@Entity(name = ActionContextEntity_.ENTITY)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionContextEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = ActionContextEntity_.ID_COLUMN, nullable = false, unique = true)
    private UUID id;

    @Column(name = ActionContextEntity_.METADATA_ID_COLUMN, nullable = false, insertable = false, updatable = false)
    private UUID metadataId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = ActionContextEntity_.METADATA_ID_COLUMN, nullable = false)
    private ActionMetadataEntity metadata;

    @Column(name = ActionContextEntity_.CORRELATION_ID_COLUMN, nullable = false, unique = true)
    private String correlationId;

    @Column(name = ActionContextEntity_.ITEM_ID_COLUMN)
    private String itemId;

    @OneToMany(mappedBy = ActionProcessEntity_.CONTEXT_RELATION)
    private Set<ActionProcessEntity> processes;

    public Optional<ActionMetadataEntity> getMetadata( )
    {
        return Optional.ofNullable(this.metadata);
    }

    public Optional<String> getItemId( )
    {
        return Optional.ofNullable(this.itemId);
    }

    public Optional<Set<ActionProcessEntity>> getProcesses( )
    {
        return Optional.ofNullable(this.processes);
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
