package customErrors;

public class MissingTableException extends RuntimeException {
    public MissingTableException(String message) {
        super(message);
    }
}
