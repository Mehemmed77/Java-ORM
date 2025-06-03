package customErrors;

public class AbsenceOfColumns extends RuntimeException {
    public AbsenceOfColumns(String message) {
        super(message);
    }
}
