package assignment.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal comma-separated helper for record serialisation.
 * Field values must not themselves contain the delimiters or newlines;
 * {@link #join(Object...)} strips them defensively.
 */
public final class CsvUtil {

    /** Field delimiter between columns of one record. */
    public static final String DELIM = ",";
    /** Secondary delimiter used inside a single list-valued field. */
    public static final String SUBDELIM = ";";

    private CsvUtil() { }

    public static String join(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(DELIM);
            sb.append(fields[i] == null ? "" : sanitize(String.valueOf(fields[i])));
        }
        return sb.toString();
    }

    /** Splits a record, keeping trailing empty fields. */
    public static String[] split(String line) {
        return line.split(DELIM, -1);
    }

    public static String joinList(List<String> values) {
        return String.join(SUBDELIM, values);
    }

    public static List<String> splitList(String value) {
        List<String> out = new ArrayList<>();
        if (value == null || value.isBlank()) return out;
        for (String s : value.split(SUBDELIM)) {
            if (!s.isBlank()) out.add(s.trim());
        }
        return out;
    }

    private static String sanitize(String s) {
        return s.replace(DELIM, " ")
                .replace(SUBDELIM, " ")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
