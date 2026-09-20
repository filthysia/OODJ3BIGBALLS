package assignment.model;

/** Operational state of a physical hospital asset. */
public enum AssetStatus {
    AVAILABLE, ALLOCATED, MAINTENANCE, RETIRED;

    public static AssetStatus fromString(String s) {
        if (s != null) {
            for (AssetStatus x : values()) {
                if (x.name().equalsIgnoreCase(s.trim())) return x;
            }
        }
        return AVAILABLE;
    }
}
