package ir.ac.kntu.Meowter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SessionManager {

    private static final String SESSION_FILE = "userSession.json";
    private static UserRepository userRepository = new UserRepository();

    public static void saveSession(User user) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File sessionFile = new File(SESSION_FILE);

            String checksum = computeChecksum(user);
            SessionData sessionData = new SessionData(user, checksum);

            objectMapper.writeValue(sessionFile, sessionData);
            CliFormatter.progressBar(CliFormatter.boldYellow("Saving the session..."), 10);
            CliFormatter.printTypingEffect(CliFormatter.boldGreen("Session saved successfully."));
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(CliFormatter.boldRed("Error saving session: " + e.getMessage()));
        }
    }

    public static User loadSession() {
        try {
            File sessionFile = new File(SESSION_FILE);
            if (sessionFile.exists() && sessionFile.length() > 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                SessionData sessionData = objectMapper.readValue(sessionFile, SessionData.class);

                String currentChecksum = computeChecksum(sessionData.getUser());

                if (!sessionData.getChecksum().equals(currentChecksum)) {
                    CliFormatter.loadingSpinner("⚠️ Session file has been modified! Session will be terminated.");
                    clearSession();
                    return null;
                }

                return userRepository.findByUsername(sessionData.getUser().getUsername());
            } else {
                CliFormatter.loadingSpinner(CliFormatter.boldRed("⚠️ No valid session file found!"));
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println(CliFormatter.boldRed("Error loading session: " + e.getMessage()));
        }
        return null;
    }

    public static void clearSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            sessionFile.delete();
            System.out.println(CliFormatter.boldBlue("Session cleared."));
        }
    }

    private static String computeChecksum(User user) throws NoSuchAlgorithmException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] userBytes = objectMapper.writeValueAsBytes(user);

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] checksumBytes = md.digest(userBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : checksumBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static class SessionData {
        private User user;
        private String checksum;

        public SessionData() {}

        public SessionData(User user, String checksum) {
            this.user = user;
            this.checksum = checksum;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }
    }
}
