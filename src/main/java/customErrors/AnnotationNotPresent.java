package customErrors;

public class AnnotationNotPresent extends RuntimeException {
    public AnnotationNotPresent(String message) {
        super(message);
    }
}
