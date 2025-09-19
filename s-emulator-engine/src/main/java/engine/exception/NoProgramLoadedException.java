package engine.exception;

/**
 * Exception thrown when an operation is attempted but no program is loaded.
 */
public class NoProgramLoadedException extends Exception {
    
    public NoProgramLoadedException(String message) {
        super(message);
    }
}
