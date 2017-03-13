package uk.ac.ebi.subs.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.utils.Base64Coder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.data.client.Sample;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertThat;

/**
 * Created by davidr on 24/02/2017.
 */
public class ApiIntegrationTestHelper {

    private ObjectMapper objectMapper;
    private String rootUri;
    private static String DEFAULT_USER = "user";
    private static String DEFAULT_PASSWORD = "password";

    public ApiIntegrationTestHelper(ObjectMapper objectMapper, String rootUri) {
        this.objectMapper = objectMapper;
        this.rootUri = rootUri;
    }

    public HttpResponse<JsonNode> postSubmission(Map<String, String> rootRels, Submission submission) throws UnirestException {
        //create a new submission
        HttpResponse<JsonNode> submissionResponse = Unirest.post(rootRels.get("submissions:create"))
                .headers(standardPostHeaders())
                .body(submission)
                .asJson();

        assertThat(submissionResponse.getStatus(), is(equalTo(HttpStatus.CREATED.value())));
        assertThat(submissionResponse.getHeaders().get("Location"), notNullValue());
        return submissionResponse;
    }

    public String submissionWithSamples(Map<String, String> rootRels) throws UnirestException, IOException {
        Submission submission = Helpers.generateSubmission();
        HttpResponse<JsonNode> submissionResponse = postSubmission(rootRels, submission);

        String submissionLocation = submissionResponse.getHeaders().get("Location").get(0).toString();
        Map<String, String> submissionRels = relsFromPayload(submissionResponse.getBody().getObject());

        assertThat(submissionRels.get("samples"), notNullValue());

        List<Sample> testSamples = Helpers.generateTestClientSamples(2);
        //add samples to the submission
        for (Sample sample : testSamples) {

            sample.setSubmission(submissionLocation);

            HttpResponse<JsonNode> sampleResponse = Unirest.post(rootRels.get("samples:create"))
                    .headers(standardPostHeaders())
                    .body(sample)
                    .asJson();

            assertThat(sampleResponse.getStatus(), is(equalTo(HttpStatus.CREATED.value())));
        }

        //retrieve the samples
        String submissionSamplesUrl = submissionRels.get("samples");

        HttpResponse<JsonNode> samplesQueryResponse = Unirest.get(submissionSamplesUrl)
                .headers(standardGetHeaders())
                .asJson();

        assertThat(samplesQueryResponse.getStatus(), is(equalTo(HttpStatus.OK.value())));

        JSONObject payload = samplesQueryResponse.getBody().getObject();
        JSONArray sampleList = payload.getJSONObject("_embedded").getJSONArray("samples");

        assertThat(sampleList.length(), is(equalTo(testSamples.size())));
        return submissionLocation;
    }

    public Map<String, String> rootRels() throws UnirestException, IOException {
        HttpResponse<JsonNode> response = Unirest.get(rootUri)
                .headers(standardGetHeaders())
                .asJson();

        assertThat(response.getStatus(), is(equalTo(HttpStatus.OK.value())));
        JSONObject payload = response.getBody().getObject();

        return relsFromPayload(payload);
    }

    public Map<String, String> relsFromPayload(JSONObject payload) throws IOException {
        assertThat((Set<String>) payload.keySet(), hasItem("_links"));

        JSONObject links = payload.getJSONObject("_links");

        Map<String, String> rels = new HashMap<>();


        for (Object key : links.keySet()) {

            assertThat(key.getClass(), typeCompatibleWith(String.class));

            Object linkJson = links.get(key.toString());
            Link link = objectMapper.readValue(linkJson.toString(), Link.class);

            rels.put((String) key, link.getHref());

        }
        return rels;
    }

    public static Map<String, String> standardGetHeaders() {
        Map<String, String> h = new HashMap<>();
        h.put("accept", MediaTypes.HAL_JSON_VALUE);
        h.put("Authorization", "Basic " + Base64Coder.encodeString(DEFAULT_USER + ":" + DEFAULT_PASSWORD));
        return h;
    }

    public static Map<String, String> standardPostHeaders() {
        Map<String, String> h = new HashMap<>();
        h.put("accept", MediaTypes.HAL_JSON_VALUE);
        h.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        h.put("Authorization", "Basic " + Base64Coder.encodeString(DEFAULT_USER + ":" + DEFAULT_PASSWORD));
        return h;
    }

}
