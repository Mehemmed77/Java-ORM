package customErrors;

public class MissingColumnsException extends RuntimeException {
    public MissingColumnsException(String message) {
        super(message);
    }
}
