package assignment.model;

/** Payment state of an invoice (used by revenue summary reports). */
public enum PaymentStatus {
    PAID, UNPAID, PARTIAL;

    public static PaymentStatus fromString(String s) {
        if (s != null) {
            for (PaymentStatus x : values()) {
                if (x.name().equalsIgnoreCase(s.trim())) return x;
            }
        }
        return UNPAID;
    }
}
