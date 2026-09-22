package assignment.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Thin wrapper around plain-text file storage (one record per line). */
public final class FileHandler {

    private FileHandler() { }

    /** Reads every line of a file. Returns an empty list if the file does not exist. */
    public static List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return lines;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
        return lines;
    }

    /** Overwrites a file with the supplied lines, creating parent folders as needed. */
    public static void writeLines(String path, List<String> lines) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file, false)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + path, e);
        }
    }

    /** Appends a single record to the end of a file. */
    public static void appendLine(String path, String line) {
        List<String> lines = readLines(path);
        lines.add(line);
        writeLines(path, lines);
    }
}
