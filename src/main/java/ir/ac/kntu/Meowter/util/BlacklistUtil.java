package ir.ac.kntu.Meowter.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BlacklistUtil {

    private static final String BLACKLIST_FILE = "resources/blacklist.txt";

    public static boolean containsBlacklistedWord(String content) {
        try {
            List<String> blacklist = Files.readAllLines(Paths.get(BLACKLIST_FILE));
            for (String word : blacklist) {
                if (content.toLowerCase().contains(word.toLowerCase())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading blacklist file: " + e.getMessage());
        }
        return false;
    }
}
