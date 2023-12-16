package de.cybine.stuvapi.relay.util.converter;

import com.fasterxml.jackson.annotation.*;
import de.cybine.stuvapi.relay.util.converter.ConverterConstraint.*;
import lombok.*;

import java.lang.reflect.*;
import java.util.*;

@Getter
@ToString
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({ "unused", "BooleanMethodIsAlwaysInverted" })
public class ConverterTreeNode
{
    @JsonProperty("id")
    @Builder.Default
    private final UUID id = UUID.randomUUID();

    @JsonProperty("item_type")
    private final Class<?> itemType;

    @JsonProperty("item_id")
    private final Object itemId;

    @JsonProperty("parent_node_id")
    private final UUID parentNodeId;

    @JsonProperty("child_node_ids")
    private final Set<UUID> childNodeIds = new HashSet<>();

    @JsonIgnore
    @ToString.Exclude
    private final ConverterTree tree;

    public Optional<Object> getItemId( )
    {
        return Optional.ofNullable(this.itemId);
    }

    public Optional<UUID> getParentNodeId( )
    {
        return Optional.ofNullable(this.parentNodeId);
    }

    @JsonIgnore
    public ConverterConstraint getConstraint( )
    {
        return this.tree.getConstraint();
    }

    public ConverterConstraint getConstraint(Type type)
    {
        return this.tree.getConstraint(type);
    }

    public boolean hasChildNodes( )
    {
        return !this.childNodeIds.isEmpty();
    }

    public Set<UUID> getChildNodeIds( )
    {
        return Collections.unmodifiableSet(this.childNodeIds);
    }

    public int getDepth( )
    {
        return this.getDepth(null);
    }

    public int getDepth(Type type)
    {
        int depth = 0;
        ConverterTreeNode node = this;
        while (node != null)
        {
            if (node.itemType == null || node.itemType == ConverterTreeRootNodeType.class)
            {
                node = node.getParentNodeId().map(this.tree::getNodeOrThrow).orElse(null);
                continue;
            }

            if (type == null || type == node.itemType)
            {
                depth++;
            }

            node = node.getParentNodeId().map(this.tree::getNodeOrThrow).orElse(null);
        }

        return depth;
    }

    public boolean hasBeenProcessed(Object item)
    {
        if (item == null)
        {
            throw new IllegalArgumentException("Item cannot be null");
        }

        Object objectId = this.tree.findItemId(item).orElse(null);
        if (objectId == null)
        {
            return false;
        }

        return this.hasBeenProcessed(item.getClass(), objectId);
    }

    private boolean hasBeenProcessed(Type type, Object itemId)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Type cannot be null");
        }

        if (itemId == null)
        {
            throw new IllegalArgumentException("ItemId cannot be null");
        }

        if (this.itemType == type && Objects.equals(this.itemId, itemId))
        {
            return true;
        }

        if (this.parentNodeId == null)
        {
            return false;
        }

        return this.tree.getNodeOrThrow(this.parentNodeId).hasBeenProcessed(type, itemId);
    }

    public boolean shouldBeProcessed(Type type)
    {
        int generalMaxDepth = this.getConstraint().getMaxDepth().orElse(Integer.MAX_VALUE);
        if (this.getDepth() >= generalMaxDepth)
        {
            return false;
        }

        return this.getDepth(type) < this.getConstraint(type).getMaxDepth().orElse(generalMaxDepth);
    }

    public boolean shouldBeProcessed(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        ConverterConstraint constraint = this.getConstraint();
        if (this.getDepth() >= constraint.getMaxDepth().orElse(Integer.MAX_VALUE))
        {
            return false;
        }

        DuplicatePolicy generalDuplicatePolicy = constraint.getDuplicatePolicy().orElse(DuplicatePolicy.IGNORE_ALL);
        if (obj instanceof Collection<?> collection)
        {
            ConverterConstraint typeConstraint = this.getConstraint(
                    collection.stream().filter(Objects::nonNull).map(Object::getClass).findAny().orElse(null));

            switch (typeConstraint.getDuplicatePolicy().orElse(generalDuplicatePolicy))
            {
                case IGNORE_ALL ->
                {
                    return collection.parallelStream().filter(Objects::nonNull).noneMatch(this::hasBeenProcessed);
                }

                case IGNORE_ITEM ->
                {
                    return !collection.parallelStream().filter(Objects::nonNull).allMatch(this::hasBeenProcessed);
                }

                case PROCESS ->
                {
                    return true;
                }
            }
        }

        if (!this.shouldBeProcessed(obj.getClass()))
        {
            return false;
        }

        if (this.getConstraint(obj.getClass())
                .getDuplicatePolicy()
                .orElse(generalDuplicatePolicy) == DuplicatePolicy.PROCESS)
        {
            return true;
        }

        return !this.hasBeenProcessed(obj);
    }

    public Optional<ConverterTreeNode> process(Object item)
    {
        if (!this.shouldBeProcessed(item))
        {
            return Optional.empty();
        }

        return Optional.of(this.forceProcess(item));
    }

    public ConverterTreeNode forceProcess(Object item)
    {
        if (item == null)
        {
            throw new IllegalArgumentException("Item cannot be null");
        }

        ConverterTreeNode node = ConverterTreeNode.builder()
                                                  .tree(this.tree)
                                                  .itemId(this.tree.findItemId(item).orElse(null))
                                                  .itemType(item.getClass())
                                                  .parentNodeId(this.id)
                                                  .build();

        this.tree.addNode(node);
        this.childNodeIds.add(node.getId());

        return node;
    }
}
