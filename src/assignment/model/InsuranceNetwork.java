package assignment.model;

import assignment.util.CsvUtil;

/** An insurance network the clinic accepts, with the share of a bill it covers. */
public class InsuranceNetwork {

    private String code;
    private String name;
    private double coveragePercent;   // 0..100
    private boolean active;

    public InsuranceNetwork() { }

    public InsuranceNetwork(String code, String name, double coveragePercent, boolean active) {
        this.code = code;
        this.name = name;
        this.coveragePercent = coveragePercent;
        this.active = active;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String toCsv() {
        return CsvUtil.join(code, name, coveragePercent, active);
    }

    public static InsuranceNetwork fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new InsuranceNetwork(f[0], f[1],
                f[2].isBlank() ? 0.0 : Double.parseDouble(f[2]),
                f.length > 3 && Boolean.parseBoolean(f[3]));
    }

    @Override
    public String toString() {
        return String.format("%-8s %-28s %5.1f%% %s",
                code, name, coveragePercent, active ? "active" : "inactive");
    }
}
