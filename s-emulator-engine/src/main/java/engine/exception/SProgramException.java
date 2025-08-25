package engine.exception;

public class SProgramException extends Exception {
    public SProgramException(String message) {
        super(message);
    }

    public SProgramException(String message, Throwable cause) {
        super(message, cause);
    }
}
