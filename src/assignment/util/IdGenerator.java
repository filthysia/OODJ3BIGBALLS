package assignment.util;

import java.util.Collection;

/** Generates sequential, prefixed identifiers such as {@code DEP0007}. */
public final class IdGenerator {

    private IdGenerator() { }

    /**
     * @param prefix      literal prefix, e.g. {@code "RST"}
     * @param existingIds all identifiers already in use for that entity
     * @return next free identifier: prefix + 4-digit zero-padded counter
     */
    public static String next(String prefix, Collection<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            if (id != null && id.startsWith(prefix)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                    // ignore ids that don't follow the numeric convention
                }
            }
        }
        return String.format("%s%04d", prefix, max + 1);
    }
}
