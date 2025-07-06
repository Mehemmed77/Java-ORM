package customErrors;

public class MissingPrimaryKeyException extends RuntimeException {
    public MissingPrimaryKeyException(String message) {
        super(message);
    }
}
