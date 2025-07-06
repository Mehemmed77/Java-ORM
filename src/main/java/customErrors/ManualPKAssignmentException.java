package customErrors;

public class ManualPKAssignmentException extends RuntimeException {
    public ManualPKAssignmentException(String message) {
        super(message);
    }
}
