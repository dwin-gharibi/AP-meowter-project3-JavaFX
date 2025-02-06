package ir.ac.kntu.Meowter.exceptions;

public class PasswordValidationException extends RuntimeException {
    public PasswordValidationException(String password) {
        super("Given password is incorrect. Please try again.");
    }
}
