package common_lib.exceptions;

import io.jsonwebtoken.JwtException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maps duplicate-resource failures to HTTP 409 with the domain error code and message.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(e.getErrorCode(), e.getMessage())
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.of("INVALID_JWT", "The JWT Token is invalid, expired or malformed")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of("INVALID_REQUEST", e.getMessage())
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of("INVALID_REQUEST", e.getMessage())
        );
    }

    /**
     * Maps missing-resource failures to HTTP 404 and derives the response code from the resource type.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        String errorCode = e.getResource().toUpperCase() + "_NOT_FOUND";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.of(errorCode, e.getMessage())
        );
    }

    /**
     * Maps attempts to use a disabled resource to HTTP 409 and includes its identifier in the payload.
     */
    @ExceptionHandler(ApiKeyDisabledException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyDisabledException(ApiKeyDisabledException e) {
        String errorCode = e.getResource().toUpperCase() + "_DISABLED";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(errorCode, e.getMessage(), (String) e.getIdentifier(), null, e.getResource())
        );
    }

    /**
     * Maps a rejected business operation to HTTP 409 with its resource identity and domain error code.
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(BusinessRuleViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.of(e.getErrorCode(), e.getErrorMessage(), e.getIdentifier().toString(), null, e.getResource())
        );
    }

    /**
     * Maps a disallowed state transition to HTTP 403 with the stable {@code INVALID_STATE_TRANSITION} code.
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(InvalidStateTransitionException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.of("INVALID_STATE_TRANSITION", e.getMessage())
        );
    }

    /* Exceptions thrown by @Valid will be handled here */

    /**
     * Maps bean-validation failures to HTTP 400 and includes each rejected field and its validation message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage())).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of("VALIDATION_FAILED", "Request validation failed", fieldErrors)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of("BAD_REQUEST", e.getMessage())
        );
    }

    /**
     * Maps rate-limit rejection to HTTP 429 and reports remaining allowance, retry delay, and reset epoch.
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining", String.valueOf(e.getRequestsRemaining()))
                .header("X-Retry-After", String.valueOf(e.getRetryAfterSeconds()))
                .header("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(e.getRetryAfterSeconds()).getEpochSecond()))   //Why???
                .body(ErrorResponse.of("RATE_LIMIT_EXCEEDED", e.getMessage()));
    }

//    /** Maps uncaught exceptions to HTTP 500 using the generic error code and exception message. */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ErrorResponse.of("ERROR", e.getMessage()));
//    }
}
