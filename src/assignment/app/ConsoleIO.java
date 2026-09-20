package assignment.app;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Small shared console-input helper for the role menu apps. */
final class ConsoleIO {

    private static final Scanner IN = new Scanner(System.in);

    private ConsoleIO() { }

    static String line(String prompt) {
        System.out.print(prompt);
        return IN.nextLine().trim();
    }

    static String required(String prompt) {
        while (true) {
            String s = line(prompt);
            if (!s.isBlank()) return s;
            System.out.println("  This field is required.");
        }
    }

    static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(line(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    static Integer readIntOrNull(String prompt) {
        String s = line(prompt);
        if (s.isBlank()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("  Not a number - keeping the current value.");
            return null;
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(line(prompt));
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid amount.");
            }
        }
    }

    static Double readDoubleOrNull(String prompt) {
        String s = line(prompt);
        if (s.isBlank()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("  Not a number - keeping the current value.");
            return null;
        }
    }

    static LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(line(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("  Use the format YYYY-MM-DD.");
            }
        }
    }

    static LocalDate readDateOrNull(String prompt) {
        String s = line(prompt);
        if (s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            System.out.println("  Bad date - keeping the current value.");
            return null;
        }
    }

    static LocalTime readTime(String prompt) {
        while (true) {
            try {
                return LocalTime.parse(line(prompt));
            } catch (DateTimeParseException e) {
                System.out.println("  Use the format HH:MM (24-hour).");
            }
        }
    }

    static boolean confirm(String prompt) {
        return line(prompt + " (y/n): ").equalsIgnoreCase("y");
    }

    static void pause() {
        line("Press Enter to continue...");
    }
}
