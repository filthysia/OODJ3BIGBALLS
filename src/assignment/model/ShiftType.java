package assignment.model;

import java.time.LocalTime;

/** Standard operating shift bands used when building a doctor roster. */
public enum ShiftType {
    MORNING(LocalTime.of(8, 0), LocalTime.of(14, 0)),
    AFTERNOON(LocalTime.of(14, 0), LocalTime.of(20, 0)),
    NIGHT(LocalTime.of(20, 0), LocalTime.of(23, 59));

    private final LocalTime defaultStart;
    private final LocalTime defaultEnd;

    ShiftType(LocalTime defaultStart, LocalTime defaultEnd) {
        this.defaultStart = defaultStart;
        this.defaultEnd = defaultEnd;
    }

    public LocalTime getDefaultStart() { return defaultStart; }

    public LocalTime getDefaultEnd() { return defaultEnd; }

    public static ShiftType fromString(String s) {
        if (s != null) {
            for (ShiftType t : values()) {
                if (t.name().equalsIgnoreCase(s.trim())) return t;
            }
        }
        throw new IllegalArgumentException("Unknown shift type: " + s);
    }
}
