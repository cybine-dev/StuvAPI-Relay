package de.cybine.stuvapi.relay.data.lecture;

import lombok.*;

import java.util.*;

@Getter
@RequiredArgsConstructor
public enum LectureType
{
    ONLINE(1), PRESENCE(2), HYBRID(3);

    private final int typeId;

    public static Optional<LectureType> findTypeId(int typeId)
    {
        return Arrays.stream(LectureType.values()).filter(item -> item.getTypeId() == typeId).findAny();
    }
}
