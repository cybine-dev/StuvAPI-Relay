package de.cybine.stuvapi.relay.api.v1;

import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import de.cybine.stuvapi.relay.data.lecture.LectureRepository;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;

@ApplicationScoped
@AllArgsConstructor
public class LectureEndpointImpl implements LectureEndpoint
{
    private LectureRepository lectureRepository;

    @Override
    public Collection<LectureDto> fetchAll(LocalDateTime from, LocalDateTime until, String course)
    {
        return this.lectureRepository.getAllLectures(course, from, until);
    }

    @Override
    public LectureDto fetch(UUID id)
    {
        return this.lectureRepository.getLectureById(id)
                .orElseThrow(( ) -> new NoSuchElementException(String.format("No lecture found for id '%s'", id)));
    }
}
