package customErrors;

public class EmptyInsertException extends RuntimeException {
    public EmptyInsertException(String message) {
        super(message);
    }
}
