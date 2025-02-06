package ir.ac.kntu.Meowter.exceptions;

public class EmailValidationException extends RuntimeException {
    public EmailValidationException(String email) {
        super("Given email " + email + " is not valid");
    }
}
