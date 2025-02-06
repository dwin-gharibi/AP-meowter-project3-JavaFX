package ir.ac.kntu.Meowter.exceptions;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException(String message) {
        super("Weak Password: " + message);
    }
}
