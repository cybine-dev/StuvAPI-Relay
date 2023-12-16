package de.cybine.stuvapi.relay.service.stuv;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import de.cybine.stuvapi.relay.config.*;
import de.cybine.stuvapi.relay.data.lecture.*;
import de.cybine.stuvapi.relay.exception.*;
import jakarta.enterprise.context.*;
import lombok.*;
import lombok.extern.log4j.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

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
     * @param includeArchived
     *         if past lectures should be included
     *
     * @return list of fetched lectures
     *
     * @throws StuvApiFetchException
     *         indicates error during data retrieval
     * @throws InterruptedException
     *         indicates request interruption
     */
    public List<LectureData> fetchLectures(boolean includeArchived)
            throws StuvApiFetchException, InterruptedException, JsonProcessingException
    {
        return this.fetchLectures(includeArchived, item -> true);
    }

    /**
     * Fetches lectures from StuvAPI
     *
     * @param includeArchived
     *         if past lectures should be included
     * @param filter
     *         additional filter
     *
     * @return list of fetched lectures
     *
     * @throws StuvApiFetchException
     *         indicates error during data retrieval
     * @throws InterruptedException
     *         indicates request interruption
     */
    public List<LectureData> fetchLectures(boolean includeArchived, Predicate<LectureData> filter)
            throws StuvApiFetchException, InterruptedException, JsonProcessingException
    {
        URI uri = URI.create(
                String.format("%s/%s?archived=%s", this.config.baseUrl(), "rapla/lectures", includeArchived));

        String response = this.performRequest(uri).body();

        JavaType type = this.objectMapper.getTypeFactory().constructParametricType(List.class, LectureData.class);
        List<LectureData> responseData = this.objectMapper.readValue(response, type);

        return responseData.stream().filter(filter).toList();
    }

    /**
     * Performs http request
     *
     * @param uri
     *         request location
     *
     * @return http request result
     *
     * @throws StuvApiFetchException
     *         indicates error during data retrieval
     * @throws InterruptedException
     *         indicates request interruption
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
                throw new StuvApiFetchException(
                        String.format("Could not query %s: Received invalid status code %s!", uri,
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
