package assignment.model;

/** Lifecycle state of a single rostered shift. */
public enum ShiftStatus {
    SCHEDULED, COMPLETED, CANCELLED;

    public static ShiftStatus fromString(String s) {
        if (s != null) {
            for (ShiftStatus x : values()) {
                if (x.name().equalsIgnoreCase(s.trim())) return x;
            }
        }
        return SCHEDULED;
    }
}
