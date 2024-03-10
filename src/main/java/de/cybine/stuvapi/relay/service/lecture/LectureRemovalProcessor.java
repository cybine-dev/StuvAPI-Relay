package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import io.quarkus.arc.*;
import jakarta.persistence.*;
import lombok.experimental.*;

@UtilityClass
public class LectureRemovalProcessor
{
    public ActionResult<LectureData> apply(Action action, ActionHelper helper)
    {
        EntityManager entityManager = Arc.container().select(EntityManager.class).get();

        LectureData data = action.<LectureData>getData().orElseThrow().value();
        entityManager.createNativeQuery(String.format("UPDATE %s SET %s = :status WHERE %s = :id", LectureEntity_.TABLE,
                             LectureEntity_.STATUS_COLUMN, LectureEntity_.LECTURE_ID_COLUMN), Void.class)
                     .setParameter("status", LectureStatus.ARCHIVED.name())
                     .setParameter("id", data.getId())
                     .executeUpdate();

        return helper.createResult(data);
    }
}
