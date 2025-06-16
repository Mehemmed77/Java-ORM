package customErrors;

public class NoEmptyParameterConstructorException extends RuntimeException {
    public NoEmptyParameterConstructorException(String message) {
        super(message);
    }
}
