package engine.exception;

public class XMLValidationException extends SProgramException {
    public XMLValidationException(String message) {
        super(message);
    }

    public XMLValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
