package customErrors;

public class PrimaryKeyNameMismatchException extends RuntimeException {
    public PrimaryKeyNameMismatchException(String message) {
        super(message);
    }
}
