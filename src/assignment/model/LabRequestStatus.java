package assignment.model;

/** Workflow state of a lab / imaging request. */
public enum LabRequestStatus {
    REQUESTED, SCHEDULED, COMPLETED, CANCELLED;

    public static LabRequestStatus fromString(String s) {
        if (s != null) {
            for (LabRequestStatus x : values()) {
                if (x.name().equalsIgnoreCase(s.trim())) return x;
            }
        }
        return REQUESTED;
    }
}
