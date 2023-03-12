package service;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import constants.ExercisesConstants;
import dto.GetSnatch;
import dto.PRPojo;
import dto.PostSnatch;
import exception.ExerciseErrorResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(WireMockExtension.class)
public class ExercisesRestClientWireMockServerFaultTest {

    ExercisesRestClient exercisesRestClient;
    WebClient webClient;

    @InjectServer
    WireMockServer server;

    @ConfigureWireMock
    Options options = wireMockConfig().dynamicPort().port(9998).notifier(new ConsoleNotifier(true))
            .extensions(new ResponseTemplateTransformer(true));

    TcpClient tcpClient = TcpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 12000).doOnConnected(
            connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5));
            }
    );

    @BeforeEach
    void setUp() {
        int port = server.port();
        String baseUrl = String.format("http://localhost:%s", port);
        System.out.println("baseUrl: " + baseUrl);
        webClient = WebClient.create(baseUrl);
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(baseUrl).build();
        exercisesRestClient = new ExercisesRestClient(webClient);
    }

    @Test
    void retrieveAllExercisesError(){
//        given
        stubFor(get(anyUrl()).willReturn(serverError()));

//        when

//        then
        assertThrows(ExerciseErrorResponse.class, () -> exercisesRestClient.retrieveExerciseList());
    }

    @Test
    void retrieveAllExercises_503_serviceUnavailable(){
//        given
        stubFor(get(anyUrl()).willReturn(serverError().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

//        when
//        then
        assertThrows(ExerciseErrorResponse.class, () -> exercisesRestClient.retrieveExerciseList());
    }


    @Test
    void retrieveAllExercises_fault_response(){
//        given
        stubFor(get(anyUrl()).willReturn(serverError().withFault(Fault.EMPTY_RESPONSE)));

//        when
//        then
        String errorResponseString = ":reactor.core.Exceptions$ReactiveException: " +
                "reactor.netty.http.client.PrematureCloseException: Connection prematurely closed BEFORE response";
        ExerciseErrorResponse exerciseErrorResponse =
                assertThrows(ExerciseErrorResponse.class, () -> exercisesRestClient.retrieveExerciseList());
        assertEquals(exerciseErrorResponse, exerciseErrorResponse.getMessage());
    }

    @Test
    void retrieveAllMovies_fixedDelay(){
//        given
        stubFor(get(anyUrl()).willReturn(ok().withFixedDelay(6000)));

//        when
//        then
        assertThrows(ExerciseErrorResponse.class, () -> exercisesRestClient.retrieveExerciseList());
    }

    @Test
    void retrieveAllMovies_randomDelay(){
//        given
        stubFor(get(anyUrl()).willReturn(ok().withUniformRandomDelay(6000, 10000)));

//        when
//        then
        assertThrows(ExerciseErrorResponse.class, () -> exercisesRestClient.retrieveExerciseList());
    }

}
