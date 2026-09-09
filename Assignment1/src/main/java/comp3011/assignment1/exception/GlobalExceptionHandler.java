package comp3011.assignment1.exception;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalState(
	        IllegalStateException exception,
	        HttpServletRequest request) {

	    return buildErrorResponse(
	            HttpStatus.INTERNAL_SERVER_ERROR,
	            exception.getMessage(),
	            request.getRequestURI()
	    );
	}

	@ExceptionHandler(RestClientException.class)
	public ResponseEntity<Map<String, Object>> handleRestClientException(
	        RestClientException exception,
	        HttpServletRequest request) {

	    return buildErrorResponse(
	            HttpStatus.BAD_GATEWAY,
	            "Cloud transcription service request failed.",
	            request.getRequestURI()
	    );
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, Object>> handleIOException(
	        IOException exception,
	        HttpServletRequest request) {

	    return buildErrorResponse(
	            HttpStatus.INTERNAL_SERVER_ERROR,
	            "Unable to process the uploaded audio file.",
	            request.getRequestURI()
	    );
	}
    
    @ExceptionHandler(ShutdownAlreadyInProgressException.class)
    public ResponseEntity<Map<String, Object>> handleShutdownAlreadyInProgress(
            ShutdownAlreadyInProgressException exception,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String path) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}