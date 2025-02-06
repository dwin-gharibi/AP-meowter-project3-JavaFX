package ir.ac.kntu.Meowter.util;

import ir.ac.kntu.Meowter.exceptions.InvalidUsernameException;
import ir.ac.kntu.Meowter.exceptions.InvalidEmailException;
import ir.ac.kntu.Meowter.exceptions.WeakPasswordException;
import ir.ac.kntu.Meowter.repository.UserRepository;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final UserRepository userRepository = new UserRepository();

    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUsernameException("Username cannot be empty.");
        }

        if (userRepository.findByUsername(username) != null) {
            throw new InvalidUsernameException("Username already exists.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email cannot be empty.");
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new InvalidEmailException("Invalid email format.");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new InvalidEmailException("Email already exists.");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty() || password.length() < 8) {
            throw new WeakPasswordException("Password cannot be empty.");
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$";
        if (!Pattern.matches(passwordRegex, password)) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }
    }

}
