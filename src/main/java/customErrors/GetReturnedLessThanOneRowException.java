package customErrors;

public class GetReturnedLessThanOneRowException extends RuntimeException {
    public GetReturnedLessThanOneRowException(String message) {
        super(message);
    }
}
