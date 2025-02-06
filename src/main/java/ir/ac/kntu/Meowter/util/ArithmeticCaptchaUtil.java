package ir.ac.kntu.Meowter.util;

import java.util.Random;
import java.util.Scanner;
import ir.ac.kntu.Meowter.exceptions.CaptchaVerificationException;

public class ArithmeticCaptchaUtil {

    private static final Random RANDOM = new Random();

    public static String[] generateArithmeticCaptcha() {
        int num1 = RANDOM.nextInt(10) + 1;
        int num2 = RANDOM.nextInt(10) + 1;

        char[] operators = {'+', '-', '*'};
        char operator = operators[RANDOM.nextInt(operators.length)];

        String question;
        int result;

        switch (operator) {
            case '+':
                result = num1 + num2;
                question = num1 + " + " + num2;
                break;
            case '-':
                result = num1 - num2;
                question = num1 + " - " + num2;
                break;
            case '*':
                result = num1 * num2;
                question = num1 + " * " + num2;
                break;
            default:
                throw new IllegalStateException("Unexpected operator: " + operator);
        }

        return new String[]{question, String.valueOf(result)};
    }

    public static boolean verifyCaptcha(String expectedResult, String userAnswer) {
        return expectedResult.equals(userAnswer.trim());
    }

    public static void askCaptchaOrThrow() throws CaptchaVerificationException {
        Scanner scanner = new Scanner(System.in);

        String[] captcha = generateArithmeticCaptcha();
        String question = captcha[0];
        String expectedResult = captcha[1];

        System.out.println(CliFormatter.boldRed("=== CAPTCHA Verification ==="));
        System.out.println("Solve the following CAPTCHA: " + CliFormatter.boldGreen(question));
        System.out.print(CliFormatter.boldBlue("Your Answer: "));
        String userInput = scanner.nextLine();

        if (!verifyCaptcha(expectedResult, userInput)) {
            throw new CaptchaVerificationException("CAPTCHA verification failed. Please try again.");
        }

        System.out.println(CliFormatter.boldBlue("CAPTCHA Verified Successfully!"));
    }

}


