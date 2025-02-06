package ir.ac.kntu.Meowter.exceptions;

public class CaptchaVerificationException extends RuntimeException {
    public CaptchaVerificationException(String message) {
        super("Captcha verification failed: " + message);
    }
}
