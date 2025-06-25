package customErrors;

public class ReferencedColumnMissingException extends RuntimeException {
    public ReferencedColumnMissingException(String message) {
        super(message);
    }
}
