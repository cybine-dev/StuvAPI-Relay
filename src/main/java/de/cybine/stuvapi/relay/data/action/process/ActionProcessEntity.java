package de.cybine.stuvapi.relay.data.action.process;

import de.cybine.stuvapi.relay.data.action.context.*;
import de.cybine.stuvapi.relay.service.action.data.*;
import de.cybine.stuvapi.relay.util.*;
import jakarta.persistence.*;
import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;

@Data
@NoArgsConstructor
@Builder(builderClassName = "Generator")
@Table(name = ActionProcessEntity_.TABLE)
@Entity(name = ActionProcessEntity_.ENTITY)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionProcessEntity implements Serializable, WithId<UUID>
{
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = ActionProcessEntity_.ID_COLUMN, nullable = false, unique = true)
    private UUID id;

    @Column(name = ActionProcessEntity_.EVENT_ID_COLUMN, nullable = false, unique = true)
    private String eventId;

    @Column(name = ActionProcessEntity_.CONTEXT_ID_COLUMN, nullable = false, insertable = false, updatable = false)
    private UUID contextId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = ActionProcessEntity_.CONTEXT_ID_COLUMN, nullable = false)
    private ActionContextEntity context;

    @Column(name = ActionProcessEntity_.STATUS_COLUMN, nullable = false)
    private String status;

    @Builder.Default
    @Column(name = ActionProcessEntity_.PRIORITY_COLUMN, nullable = false)
    private int priority = 100;

    @Column(name = ActionProcessEntity_.DESCRIPTION_COLUMN)
    private String description;

    @Column(name = ActionProcessEntity_.CREATOR_ID_COLUMN)
    private String creatorId;

    @Builder.Default
    @Column(name = ActionProcessEntity_.CREATED_AT_COLUMN, nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = ActionProcessEntity_.DUE_AT_COLUMN)
    private ZonedDateTime dueAt;

    @Column(name = ActionProcessEntity_.DATA_COLUMN)
    @Convert(converter = ActionDataAttributeConverter.class)
    private ActionData data;

    public Optional<ActionContextEntity> getContext( )
    {
        return Optional.ofNullable(this.context);
    }

    public Optional<String> getDescription( )
    {
        return Optional.ofNullable(this.description);
    }

    public Optional<String> getCreatorId( )
    {
        return Optional.ofNullable(this.creatorId);
    }

    public Optional<ZonedDateTime> getDueAt( )
    {
        return Optional.ofNullable(this.dueAt);
    }

    public Optional<ActionData> getData( )
    {
        return Optional.ofNullable(this.data);
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