package assignment.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Thin wrapper around plain-text file storage (one record per line). */
public final class FileHandler {

    private FileHandler() { }

    /** Reads every line of a file. Returns an empty list if the file does not exist. */
    public static List<String> readLines(String path) {
        Path p = Paths.get(path);
        if (Files.notExists(p)) return new ArrayList<>();
        try {
            return new ArrayList<>(Files.readAllLines(p, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }

    /** Overwrites a file with the supplied lines, creating parent folders as needed. */
    public static void writeLines(String path, List<String> lines) {
        Path p = Paths.get(path);
        try {
            if (p.getParent() != null) Files.createDirectories(p.getParent());
            Files.write(p, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write " + path, e);
        }
    }

    /** Appends a single record to the end of a file. */
    public static void appendLine(String path, String line) {
        List<String> lines = readLines(path);
        lines.add(line);
        writeLines(path, lines);
    }
}
