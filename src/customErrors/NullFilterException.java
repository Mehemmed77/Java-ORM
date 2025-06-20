package customErrors;

public class NullFilterException extends RuntimeException {
    public NullFilterException(String message) {
        super(message);
    }
}
