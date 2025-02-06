package ir.ac.kntu.Meowter.util;

public class CliFormatter {
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String UNDERLINE = "\033[4m";

    public static final String RED = "\033[31m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String MAGENTA = "\033[35m";
    public static final String CYAN = "\033[36m";
    public static final String WHITE = "\033[37m";
    private static final String PURPLE = "\u001B[35m";

    public static String boldBlue(String text) {
        return BOLD + BLUE + text + RESET;
    }

    public static String boldPurple(String text) {
        return BOLD + PURPLE + text + RESET;
    }

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String underline(String text) {
        return UNDERLINE + text + RESET;
    }

    public static String red(String text) {
        return RED + text + RESET;
    }

    public static String green(String text) {
        return GREEN + text + RESET;
    }

    public static String yellow(String text) {
        return YELLOW + text + RESET;
    }

    public static String blue(String text) {
        return BLUE + text + RESET;
    }

    public static String magenta(String text) {
        return MAGENTA + text + RESET;
    }

    public static String cyan(String text) {
        return CYAN + text + RESET;
    }

    public static String boldRed(String text) {
        return bold(red(text));
    }

    public static String boldGreen(String text) {
        return bold(green(text));
    }

    public static String boldYellow(String text) {
        return bold(yellow(text));
    }

    public static String blueUnderlined(String text) {
        return underline(blue(text));
    }

    public static void printMeow(){
        System.out.println("        /\\     /\\");
        System.out.println("       {  `---'  }");
        System.out.println("       {  O   O  }");
        System.out.println("       ~~>  V  <~~");
        System.out.println("        \\ \\|/ /");
        System.out.println("         `-----'____");
        System.out.println("         /     \\    \\_");
        System.out.println("        {       }\\  )_\\_   _");
        System.out.println("        |  \\_/  |/ / /   \\_/ \\");
        System.out.println("         \\__/  /(_/ /     \\__/");
        System.out.println("           (__/");
        System.out.println();
        System.out.println("   ~~~~~~~~~ Meowter ~~~~~~~~~");
    }

    public static void loadingSpinner(String message) {
        String[] spinnerFrames = {"|", "/", "-", "\\"};
        System.out.print(message + " ");
        for (int i = 0; i < 30; i++) {
            System.out.print("\r" + message + " " + spinnerFrames[i % spinnerFrames.length]);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("\nLoading interrupted.");
                return;
            }
        }
        System.out.println("\r" + message + " ✅");
    }

    public static void printTypingEffect(String message) {
        for (char c : message.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }

    public static void progressBar(String task, int totalSteps) {
        System.out.print(task + " ");
        for (int step = 0; step <= totalSteps; step++) {
            int percent = (step * 100) / totalSteps;
            int completedBars = percent / 10;
            int remainingBars = 10 - completedBars;

            String progressBar = "[" + "=".repeat(completedBars) + " ".repeat(remainingBars) + "] " + percent + "%";

            System.out.print("\r" + task + " " + progressBar);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("\nProgress interrupted.");
                return;
            }
        }
        System.out.println("\r" + task + " [==========] 100% ✅");
    }
}

