package service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import constants.ExercisesConstants;
import dto.GetSnatch;
import dto.PRPojo;
import dto.PostSnatch;
import exception.ExerciseErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WireMockExtension.class)
public class ExercisesRestClientWireMockBasicTest {

    ExercisesRestClient exercisesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer server;

    @ConfigureWireMock
    Options options = wireMockConfig().dynamicPort().port(9998).notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    @BeforeEach
    void setUp() {
        int port = server.port();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl: " + baseUrl);
        webClient = WebClient.create(baseUrl);
        exercisesRestClient = new ExercisesRestClient(webClient);
    }

    @Test
    void retrieveAllExercises(){
//        given
        stubFor(get(anyUrl()).willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("exercises.json")));

//        when
        List<PRPojo> prList = exercisesRestClient.retrieveExerciseList();
        System.out.println(prList);

//        then
        assertTrue(prList.size() > 0);
    }

    @Test
    void retrieveAllExercises_withUrl(){
//        given
        stubFor(get(urlPathEqualTo(ExercisesConstants.GET_ALL_EXERCISES))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("exercises.json")));

//        when
        List<PRPojo> prList = exercisesRestClient.retrieveExerciseList();
        System.out.println(prList);

//        then
        assertTrue(prList.size() > 0);
    }

    @Test
    void retrieveExerciseById(){
        Integer movieId = 11;

//        given
        stubFor(get(urlPathMatching("/api/exercises/[0-9]+"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile("snatch.json")));

//        when
        GetSnatch snatch = (GetSnatch) exercisesRestClient.retrieveExerciseWithId(movieId);
        System.out.println(snatch);

//        then
        assertEquals(120, snatch.getPrWeight());
    }

    @Test
    void postSnatch(){
        PostSnatch snatch = new PostSnatch("snatch", "2022-10-10", 110);
        stubFor(post(urlPathEqualTo(ExercisesConstants.GET_ALL_EXERCISES))
                .withRequestBody(matchingJsonPath(("$.exerciseName"), equalTo("snatch")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("post-snatch.json")));
//        when
        GetSnatch snatch1 = (GetSnatch) exercisesRestClient.postSnatch(snatch);

//        then
        assertEquals(110.0, snatch1.getPrWeight());
    }

    @Test
    void postSnatchWithTemplate(){
        PostSnatch snatch = new PostSnatch("snatch", "2022-10-10", 110);
        stubFor(post(urlPathEqualTo(ExercisesConstants.GET_ALL_EXERCISES))
                .withRequestBody(matchingJsonPath(("$.scoreDate"), equalTo("2022-10-10")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("post-snatch-template.json")));
//        when
        GetSnatch snatch1 = (GetSnatch) exercisesRestClient.postSnatch(snatch);

//        then
        assertEquals(110.0, snatch1.getPrWeight());
    }

    @Test
    void retrieveExerciseById_NotFound(){
        Integer movieId = 10;
        stubFor(get(urlPathMatching("/api/exercises/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("exercise404.json")));

//        when
        assertThrows(ExerciseErrorResponse.class, () -> exercisesRestClient.retrieveExerciseWithId(movieId));
    }

    @Test
    void deleteMovie(){
        PostSnatch snatch = new PostSnatch("snatch", "2022-10-10", 110);
        stubFor(post(urlPathEqualTo(ExercisesConstants.GET_ALL_EXERCISES))
                .withRequestBody(matchingJsonPath(("$.exerciseName"), equalTo("snatch")))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBodyFile("post-snatch.json")));
//        when
        GetSnatch snatch1 = (GetSnatch) exercisesRestClient.postSnatch(snatch);

//        then
        assertEquals(110.0, snatch1.getPrWeight());

        String expectedMessage = "Deleted exercise with exerciseId: 98";
        stubFor(delete(urlPathMatching("/api/exercises/[0-9]+"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.TEXT_PLAIN))
                        .withBody(expectedMessage)));
        String responseMessage = exercisesRestClient.deleteSnatch(snatch1.getId());
        assertEquals(expectedMessage, responseMessage);
    }

}
