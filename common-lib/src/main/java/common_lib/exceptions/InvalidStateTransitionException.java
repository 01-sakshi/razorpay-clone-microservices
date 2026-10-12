package common_lib.exceptions;

public class InvalidStateTransitionException extends RuntimeException {

    private final String fromState;
    private final String event;


    public InvalidStateTransitionException(String fromState, String event) {
        super("Transition from : " + fromState + " state via event: " + event + " not allowed");
        this.fromState = fromState;
        this.event = event;
    }
}
