package assignment.model;

/** Whether a clinical department is currently operating. */
public enum DepartmentStatus {
    ACTIVE, INACTIVE;

    public static DepartmentStatus fromString(String s) {
        return (s != null && s.trim().equalsIgnoreCase("INACTIVE")) ? INACTIVE : ACTIVE;
    }
}
