package assignment.model;

/** Kind of investigation a doctor can request from Admin Staff. */
public enum LabTestType {
    BLOOD_TEST(AssetType.LAB),
    URINE_TEST(AssetType.LAB),
    XRAY(AssetType.IMAGING_ROOM),
    CT_SCAN(AssetType.IMAGING_ROOM),
    MRI(AssetType.IMAGING_ROOM),
    ULTRASOUND(AssetType.IMAGING_ROOM);

    private final AssetType requiredAsset;

    LabTestType(AssetType requiredAsset) { this.requiredAsset = requiredAsset; }

    /** The asset category this test must be scheduled onto. */
    public AssetType getRequiredAsset() { return requiredAsset; }

    public static LabTestType fromString(String s) {
        if (s != null) {
            for (LabTestType t : values()) {
                if (t.name().equalsIgnoreCase(s.trim())) return t;
            }
        }
        throw new IllegalArgumentException("Unknown lab test type: " + s);
    }
}
