package common_lib.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRuleViolationException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;
    private final String resource;
    private final Object identifier;

    public BusinessRuleViolationException(String errorCode, String errorMessage, String resource, Object identifier) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.resource = resource;
        this.identifier = identifier;
    }

}
