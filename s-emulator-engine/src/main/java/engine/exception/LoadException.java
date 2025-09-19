package engine.exception;

/**
 * Exception thrown when a program file cannot be loaded.
 */
public class LoadException extends Exception {
    
    public LoadException(String message) {
        super(message);
    }
    
    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
