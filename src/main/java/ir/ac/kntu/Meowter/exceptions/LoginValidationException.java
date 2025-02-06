package ir.ac.kntu.Meowter.exceptions;

public class LoginValidationException extends RuntimeException {
    public LoginValidationException(String username) {
        super("Fail to validate username " + username);
    }
}
