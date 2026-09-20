package assignment.model;

/** Category of physical hospital asset managed by Admin Staff. */
public enum AssetType {
    CONSULTATION_ROOM, INPATIENT_WARD, LAB, IMAGING_ROOM;

    public static AssetType fromString(String s) {
        if (s != null) {
            for (AssetType t : values()) {
                if (t.name().equalsIgnoreCase(s.trim())) return t;
            }
        }
        throw new IllegalArgumentException("Unknown asset type: " + s);
    }
}
