package ir.ac.kntu.Meowter.exceptions;

public class NotExistingUserException extends RuntimeException {
    public NotExistingUserException(String message) {
        super("User " + message + " does not exist");
    }
}
