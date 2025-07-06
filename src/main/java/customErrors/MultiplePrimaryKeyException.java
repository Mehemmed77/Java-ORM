package customErrors;

public class MultiplePrimaryKeyException extends RuntimeException {
    public MultiplePrimaryKeyException(String message) {
        super(message);
    }
}
