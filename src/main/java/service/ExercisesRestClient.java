package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import constants.ExercisesConstants;
import dto.GetSnatch;
import dto.PRPojo;
import dto.PostSnatch;
import exception.ExerciseErrorResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
public class ExercisesRestClient {

    private WebClient webClient;

    public String retrieveYolo(){
        try {
            String response = webClient.get().uri(ExercisesConstants.GET_YOLO).retrieve()
                    .bodyToMono(String.class).block();
            return response;
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in RetrieveYolo. Status code is {} and the message is {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new ExerciseErrorResponse(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Exception in retrieveYolo and the message is {} ", e.getMessage() + e);
            throw new ExerciseErrorResponse(e);
        }
    }

    public PRPojo retrieveExerciseWithId(Integer id){
        try {
            PRPojo response = webClient.get().uri(ExercisesConstants.GET_EXERCISE_BY_ID, id).retrieve()
                    .bodyToMono(PRPojo.class).block();
            return response;
        } catch (WebClientResponseException e){
            log.error("WebClientResponseException in retrieveExerciseWithId. Status code is {} and he message is {}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new ExerciseErrorResponse(e.getStatusText(), e);
        } catch (Exception e){
            log.error("Exception in retrieveExerciseWithId. Message: {}", e);
            throw new ExerciseErrorResponse(e);
        }
    }

    public List<PRPojo> retrieveExerciseList() {
        try {
            List<LinkedHashMap> response = webClient.get().uri(ExercisesConstants.GET_ALL_EXERCISES).retrieve()
                    .bodyToMono(List.class).block();
            List<PRPojo> prPojos
                    = response.stream().map(s -> new ObjectMapper().convertValue(s, PRPojo.class))
                    .collect(Collectors.toList());
            return prPojos;
        } catch (WebClientResponseException ex){
            log.error("WebClientResponseException in retrieveAllExercisesList. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new ExerciseErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex){
            log.error("Exception in retrieveAllExercisesList and the message is {} ", ex.getMessage() + ex);
            throw new ExerciseErrorResponse(ex);
        }
    }

    public PRPojo postSnatch(PostSnatch snatch){
        try {
            return webClient.post().uri(ExercisesConstants.GET_ALL_EXERCISES).syncBody(snatch)
                    .retrieve().bodyToMono(GetSnatch.class).block();
        } catch(WebClientResponseException ex){
            log.error("WebClientResponseException in postSnatch. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new ExerciseErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex){
            log.error("Exception in postSnatch and the message is {} ", ex);
            throw new ExerciseErrorResponse(ex);
        }
    }

    public String deleteSnatch(Integer id) {
        try {

            return webClient.delete().uri(ExercisesConstants.GET_EXERCISE_BY_ID, id).retrieve()
                    .bodyToMono(String.class).block();
        } catch(WebClientResponseException ex){
            log.error("WebClientResponseException in deleteMovie. Status code is {} and the message is {} ", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            throw new ExerciseErrorResponse(ex.getStatusText(), ex);
        } catch (Exception ex){
            log.error("Exception in deleteMovie and the message is {} ", ex);
            throw new ExerciseErrorResponse(ex);
        }
    }
}
