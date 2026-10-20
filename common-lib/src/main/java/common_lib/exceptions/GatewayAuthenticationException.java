package common_lib.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayAuthenticationException extends RuntimeException {
    private final String errorCode;
    private final String resource;
    private final Object identifier;

    public GatewayAuthenticationException(String errorCode, String resource, String identifier, String message) {
        super(message);
        this.errorCode = errorCode;
        this.resource = resource;
        this.identifier = identifier;
    }
}
