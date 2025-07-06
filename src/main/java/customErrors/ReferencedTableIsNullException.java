package customErrors;

public class ReferencedTableIsNullException extends RuntimeException {
    public ReferencedTableIsNullException(String message) {
        super(message);
    }
}
