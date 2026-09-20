package assignment.model;

/** The four kinds of end user the Admin Staff can manage. */
public enum UserRole {
    ADMIN_STAFF("Admin Staff"),
    MEDICAL_MANAGER("Medical Manager"),
    DOCTOR("Doctor"),
    PATIENT("Patient");

    private final String label;

    UserRole(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static UserRole fromString(String s) {
        if (s != null) {
            for (UserRole r : values()) {
                if (r.name().equalsIgnoreCase(s.trim()) || r.label.equalsIgnoreCase(s.trim())) return r;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + s);
    }
}
