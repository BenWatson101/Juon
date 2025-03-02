package JUOM.UniversalObjects;

public class UniversalException extends UniversalObject {

    @Universal
    private final String message;

    public UniversalException(String message) {
        this.message = message;
    }
}
