package de.cybine.stuvapi.relay.service.lecture;

import de.cybine.quarkus.util.action.*;
import de.cybine.quarkus.util.action.data.*;
import de.cybine.quarkus.util.converter.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import io.quarkus.arc.*;
import jakarta.persistence.*;
import lombok.experimental.*;
import lombok.extern.slf4j.*;

@Slf4j
@UtilityClass
public class LectureRegistrationProcessor
{
    public static ActionResult<LectureData> apply(Action action, ActionHelper helper)
    {
        EntityManager entityManager = Arc.container().select(EntityManager.class).get();
        ConverterRegistry converterRegistry = Arc.container().select(ConverterRegistry.class).get();

        LectureData data = action.<LectureData>getData().orElseThrow().value();
        entityManager.persist(converterRegistry.getProcessor(LectureData.class)
                                               .withIntermediary(Lecture.class)
                                               .withOutput(LectureEntity.class)
                                               .withContext("lecture-registered", false)
                                               .toItem(data)
                                               .result());

        return helper.createResult(data);
    }
}
