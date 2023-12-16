package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.stuvapi.relay.data.lecture.*;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor(staticName = "of")
public class LectureDataDiff
{
    private final LectureData previous;
    private final LectureData next;

    public boolean hasDiff()
    {
        return !Objects.equals(this.previous, this.next);
    }
}
