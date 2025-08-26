package engine.exception;

public class StateSerializationException extends Exception {
    
    public StateSerializationException(String message) {
        super(message);
    }
    
    public StateSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
