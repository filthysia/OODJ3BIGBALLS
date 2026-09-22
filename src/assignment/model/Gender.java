package assignment.model;

/** Gender of a person recorded in the system. */
public enum Gender {
    MALE, FEMALE;

    public static Gender fromString(String s) {
        if (s != null) {
            for (Gender g : values()) {
                if (g.name().equalsIgnoreCase(s.trim())) return g;
            }
        }
        return MALE;
    }
}
