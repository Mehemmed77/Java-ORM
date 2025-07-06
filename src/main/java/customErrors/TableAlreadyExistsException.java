package customErrors;

public class TableAlreadyExistsException extends RuntimeException {
    public TableAlreadyExistsException(String message) {
        super(message);
    }
}
