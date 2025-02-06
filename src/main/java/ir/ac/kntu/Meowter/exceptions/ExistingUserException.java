package ir.ac.kntu.Meowter.exceptions;

public class ExistingUserException extends RuntimeException {
    public ExistingUserException(String username) {
        super("Given username " + username + " already exists");
    }
}
