package assignment.model;

/** Outcome/state of a patient appointment (used by hospital metrics reports). */
public enum AppointmentStatus {
    BOOKED, COMPLETED, CANCELLED, NO_SHOW;

    public static AppointmentStatus fromString(String s) {
        if (s != null) {
            for (AppointmentStatus x : values()) {
                if (x.name().equalsIgnoreCase(s.trim())) return x;
            }
        }
        return BOOKED;
    }
}
