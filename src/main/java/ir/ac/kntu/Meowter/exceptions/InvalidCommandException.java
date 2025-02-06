package ir.ac.kntu.Meowter.exceptions;

public class InvalidCommandException extends RuntimeException {
    public InvalidCommandException(String message) {
        super("⚠\uFE0F Invalid command: " + message);
    }
}
