package exception;


import org.springframework.web.reactive.function.client.WebClientResponseException;

public class ExerciseErrorResponse extends RuntimeException {

    public ExerciseErrorResponse(String message, WebClientResponseException e) {
        super(message, e);
    }

    public ExerciseErrorResponse(Exception e) {
        super(e);
    }
}
