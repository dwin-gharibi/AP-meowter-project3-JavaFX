package ir.ac.kntu.Meowter.util;

import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;

import java.util.List;
import java.util.Scanner;

public class PaginationUtil {

    private static final int PAGE_SIZE = 10;

    public static void paginate(List<String> data) {
        int totalPages = (int) Math.ceil((double) data.size() / PAGE_SIZE);
        int currentPage = 1;
        Scanner scanner = new Scanner(System.in);

        CliFormatter.progressBar(CliFormatter.boldYellow("Loading pages..."), 10);

        while (true) {
            displayPage(data, currentPage, totalPages);
            System.out.println();
            System.out.println("Options: " + CliFormatter.boldYellow("[n]") + "ext, " + CliFormatter.boldGreen("[p]") + "revious, " + CliFormatter.boldRed("[q]") + "uit, [Page Number]");
            System.out.println("Current Page: " + CliFormatter.blue(String.valueOf(currentPage)) +
                    " | Total Pages: " + CliFormatter.green(String.valueOf(totalPages)));
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine();

            if ("q".equalsIgnoreCase(input)) {
                System.out.println(CliFormatter.boldRed("Exiting pagination..."));
                return;
            } else if ("n".equalsIgnoreCase(input)) {
                if (currentPage < totalPages) {
                    currentPage++;
                } else {
                    System.out.println(CliFormatter.red("You are already on the last page."));
                }
            } else if ("p".equalsIgnoreCase(input)) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    System.out.println(CliFormatter.red("You are already on the first page."));
                }
            } else {
                try {
                    int pageInput = Integer.parseInt(input);
                    if (pageInput >= 1 && pageInput <= totalPages) {
                        currentPage = pageInput;
                    } else {
                        System.out.println(CliFormatter.red("Invalid page number."));
                    }
                } catch (NumberFormatException e) {
                    System.out.println(CliFormatter.red("Invalid input. Please enter a number, 'n', 'p', or 'q'."));
                }
            }
        }
    }

    private static void displayPage(List<String> data, int currentPage, int totalPages) {
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, data.size());

        System.out.println(CliFormatter.boldGreen("Page " + currentPage + " of " + totalPages));
        System.out.println(CliFormatter.bold("--------------------------------------------------"));

        for (int i = start; i < end; i++) {
            System.out.println((i + 1) + ". " + data.get(i));
        }

        System.out.println(CliFormatter.bold("--------------------------------------------------"));
    }
}
