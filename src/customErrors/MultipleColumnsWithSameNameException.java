package customErrors;

public class MultipleColumnsWithSameNameException extends RuntimeException {
    public MultipleColumnsWithSameNameException(String message) {
        super(message);
    }
}
