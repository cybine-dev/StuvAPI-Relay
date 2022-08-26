package de.cybine.stuvapi.relay.service.stuv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cybine.stuvapi.relay.config.StuvApiConfig;
import de.cybine.stuvapi.relay.data.lecture.LectureDto;
import de.cybine.stuvapi.relay.data.room.RoomDto;
import de.cybine.stuvapi.relay.exception.StuvApiFetchException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@ApplicationScoped
@AllArgsConstructor
public class StuvApi
{
    private final StuvApiConfig config;

    private final ObjectMapper objectMapper;

    /**
     * Fetches lectures from StuvAPI
     *
     * @param includeArchived if past lectures should be included
     *
     * @return list of fetched lectures
     *
     * @throws StuvApiFetchException indicates error during data retrieval
     * @throws InterruptedException  indicates request interruption
     */
    public List<LectureDto> fetchLectures(boolean includeArchived)
            throws StuvApiFetchException, InterruptedException, JsonProcessingException
    {
        URI uri = URI.create(String.format("%s/%s?archived=%s",
                this.config.baseUrl(),
                "rapla/lectures",
                includeArchived));

        String response = this.performRequest(uri).body();
        List<?> responseData = this.objectMapper.readValue(response, List.class);

        return this.parseLectureData(responseData);
    }

    /**
     * Parses result data to retrieve lectures
     *
     * @param data data to be parsed
     *
     * @return list of parsed lectures
     */
    @SuppressWarnings("unchecked")
    private List<LectureDto> parseLectureData(List<?> data)
    {
        List<LectureDto> lectures = new ArrayList<>();
        for (Object obj : data)
        {
            Map<String, Object> lectureData = (Map<String, Object>) obj;
            LectureDto lecture = LectureDto.builder()
                    .lectureId(((Integer) lectureData.get("id")).longValue())
                    .name((String) lectureData.get("name"))
                    .course((String) lectureData.get("course"))
                    .lecturer((String) lectureData.get("lecturer"))
                    .type(LectureDto.Type.valueOf((String) lectureData.get("type")))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .startsAt(LocalDateTime.parse((String) lectureData.get("startTime"),
                            DateTimeFormatter.ISO_DATE_TIME))
                    .endsAt(LocalDateTime.parse((String) lectureData.get("endTime"), DateTimeFormatter.ISO_DATE_TIME))
                    .rooms(((List<?>) lectureData.getOrDefault("rooms", Collections.emptyList())).stream()
                            .map(Object::toString)
                            .map(RoomDto.builder()::name)
                            .map(RoomDto.Builder::build)
                            .collect(Collectors.toSet()))
                    .build();

            lectures.add(lecture);
        }

        return lectures;
    }

    /**
     * Performs http request
     *
     * @param uri request location
     *
     * @return http request result
     *
     * @throws StuvApiFetchException indicates error during data retrieval
     * @throws InterruptedException  indicates request interruption
     */
    private HttpResponse<String> performRequest(URI uri) throws StuvApiFetchException, InterruptedException
    {
        try
        {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri).build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != HttpURLConnection.HTTP_OK)
            {
                throw new StuvApiFetchException(String.format("Could not query %s: Received invalid status code %s!",
                        uri,
                        response.statusCode()));
            }

            return response;
        }
        catch (IOException e)
        {
            throw new StuvApiFetchException(String.format("Could not query %s", uri), e);
        }
    }
}
