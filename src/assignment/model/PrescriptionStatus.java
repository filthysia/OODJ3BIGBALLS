package assignment.model;

/** State of a digital prescription. */
public enum PrescriptionStatus {
    ACTIVE, DISPENSED, CANCELLED;

    public static PrescriptionStatus fromString(String s) {
        if (s != null) {
            for (PrescriptionStatus x : values()) {
                if (x.name().equalsIgnoreCase(s.trim())) return x;
            }
        }
        return ACTIVE;
    }
}
