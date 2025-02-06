package ir.ac.kntu.Meowter.exceptions;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException(String message) {
        super("Invalid Username: " + message);
    }
}
