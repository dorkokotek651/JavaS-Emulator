package engine.exception;

/**
 * Exception thrown when debugger operations fail.
 */
public class DebuggerException extends Exception {
    
    public DebuggerException(String message) {
        super(message);
    }
    
    public DebuggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
