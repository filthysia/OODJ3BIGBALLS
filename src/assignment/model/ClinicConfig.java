package assignment.model;

import assignment.util.CsvUtil;

/**
 * Clinic-wide billing configuration maintained by Admin Staff.
 * Stored as a single record; a department may override {@code baseConsultationRate}
 * with its own {@link Department#getConsultationFee()}.
 */
public class ClinicConfig {

    private double baseConsultationRate;
    private double followUpRate;
    private String currency;

    public ClinicConfig() {
        this(80.0, 50.0, "RM");
    }

    public ClinicConfig(double baseConsultationRate, double followUpRate, String currency) {
        this.baseConsultationRate = baseConsultationRate;
        this.followUpRate = followUpRate;
        this.currency = currency;
    }

    public double getBaseConsultationRate() { return baseConsultationRate; }
    public void setBaseConsultationRate(double baseConsultationRate) { this.baseConsultationRate = baseConsultationRate; }

    public double getFollowUpRate() { return followUpRate; }
    public void setFollowUpRate(double followUpRate) { this.followUpRate = followUpRate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String toCsv() {
        return CsvUtil.join(baseConsultationRate, followUpRate, currency);
    }

    public static ClinicConfig fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new ClinicConfig(
                f[0].isBlank() ? 80.0 : Double.parseDouble(f[0]),
                f.length > 1 && !f[1].isBlank() ? Double.parseDouble(f[1]) : 50.0,
                f.length > 2 && !f[2].isBlank() ? f[2] : "RM");
    }

    @Override
    public String toString() {
        return String.format("base=%s%.2f  follow-up=%s%.2f", currency, baseConsultationRate,
                currency, followUpRate);
    }
}
