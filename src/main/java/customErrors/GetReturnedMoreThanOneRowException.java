package customErrors;

public class GetReturnedMoreThanOneRowException extends RuntimeException {
    public GetReturnedMoreThanOneRowException(String message) {
        super(message);
    }
}
