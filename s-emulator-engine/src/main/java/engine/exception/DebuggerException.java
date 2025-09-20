package engine.exception;

public class DebuggerException extends Exception {
    
    public DebuggerException(String message) {
        super(message);
    }
    
    public DebuggerException(String message, Throwable cause) {
        super(message, cause);
    }
}
