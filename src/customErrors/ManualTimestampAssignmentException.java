package customErrors;

public class ManualTimestampAssignmentException extends RuntimeException {
    public ManualTimestampAssignmentException(String message) {
        super(message);
    }
}
