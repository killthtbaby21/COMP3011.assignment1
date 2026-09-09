package comp3011.assignment1.exception;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException exception) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage()
        );
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(
            RestClientException exception) {

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "Cloud transcription service request failed."
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(
            IOException exception) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to process the uploaded audio file."
        );
    }
    
    @ExceptionHandler(ShutdownAlreadyInProgressException.class)
    public ResponseEntity<Map<String, Object>> handleShutdownAlreadyInProgress(
            ShutdownAlreadyInProgressException exception) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}