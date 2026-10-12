package common_lib.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Represents a structured API error with optional resource and field-level details.
 *
 * @param resource affected resource type, or {@code null} when not applicable
 * @param identifier affected resource identifier, or {@code null} when not available
 * @param errorCode stable machine-readable error code
 * @param errorDescription human-readable error description
 * @param fieldErrors validation failures, or {@code null} when not applicable
 * @param timeStamp time at which the response was created
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String resource,
        String identifier,
        String errorCode,
        String errorDescription,
        List<FieldError> fieldErrors,
        Instant timeStamp
) {
    /** Describes one rejected request field and its validation message.
     *
     * @param field rejected field name
     * @param message validation message, or {@code null} when no message was supplied
     */
    public record FieldError(
            String field,
            String message
    ) {
    }

    /**
     * Creates a timestamped error payload without resource identity or field-level validation details.
     *
     * @param errorCode stable machine-readable error code
     * @param message human-readable error description
     * @return the response payload with the current creation time
     */
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(null, null, errorCode, message, null, Instant.now());
    }

    /**
     * Creates a timestamped validation payload with the invalid field names and messages.
     *
     * @param errorCode stable machine-readable error code
     * @param message human-readable error description
     * @param fieldErrors field-level validation failures
     * @return the response payload with the current creation time
     */
    public static ErrorResponse of(String errorCode, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(null, null, errorCode, message, fieldErrors, Instant.now());
    }

    /**
     * Creates a timestamped error payload associated with a resource and optional field failures.
     *
     * @param errorCode stable machine-readable error code
     * @param message human-readable error description
     * @param identifier identifier of the affected resource, when available
     * @param fieldErrors field-level failures, or {@code null} when not applicable
     * @param resource resource type associated with the failure
     * @return the response payload with the current creation time
     */
    public static ErrorResponse of(String errorCode, String message, String identifier, List<FieldError> fieldErrors, String resource) {
        return new ErrorResponse(resource, identifier, errorCode, message, fieldErrors, Instant.now());
    }
}
