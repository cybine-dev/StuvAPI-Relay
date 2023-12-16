package de.cybine.stuvapi.relay.util.api.permission;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.jackson.*;

@Data
@Jacksonized
@Builder(builderClassName = "Generator")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiContextMapping implements Comparable<ApiContextMapping>
{
    @NotNull
    @NotBlank
    @JsonProperty("name")
    private final String name;

    @NotNull
    @NotBlank
    @JsonProperty("permission")
    private final String permission;

    @Min(1)
    @Builder.Default
    @JsonProperty("priority")
    private int priority = 100;

    @Override
    public int compareTo(ApiContextMapping other)
    {
        return Integer.compare(this.getPriority(), other.getPriority());
    }
}
