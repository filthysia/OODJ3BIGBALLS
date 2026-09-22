package assignment.service;

import assignment.model.ClinicConfig;
import assignment.model.InsuranceNetwork;

import java.util.List;

/** Use-case: Admin Staff configures base consultation rates and accepted insurance networks. */
public class BillingConfigService {

    public ClinicConfig getConfig() {
        return Database.clinicConfig;
    }

    public void setBaseConsultationRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Rate cannot be negative");
        Database.clinicConfig.setBaseConsultationRate(rate);
        Database.saveClinicConfig();
    }

    public void setFollowUpRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Rate cannot be negative");
        Database.clinicConfig.setFollowUpRate(rate);
        Database.saveClinicConfig();
    }

    public void setCurrency(String currency) {
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("Currency is required");
        Database.clinicConfig.setCurrency(currency.trim());
        Database.saveClinicConfig();
    }

    public List<InsuranceNetwork> networks() {
        return Database.insuranceNetworks;
    }

    /** @return the network with this code, or {@code null} if none matches. */
    public InsuranceNetwork findNetwork(String code) {
        String key = (code == null) ? "" : code.trim();
        for (InsuranceNetwork n : Database.insuranceNetworks) {
            if (n.getCode().equalsIgnoreCase(key)) return n;
        }
        return null;
    }

    public InsuranceNetwork addNetwork(String code, String name, double coveragePercent) {
        String c = (code == null) ? "" : code.trim().toUpperCase();
        if (c.isBlank()) throw new IllegalArgumentException("Network code is required");
        if (findNetwork(c) != null)
            throw new IllegalArgumentException("Network already exists: " + c);
        checkCoverage(coveragePercent);
        InsuranceNetwork n = new InsuranceNetwork(c, (name == null || name.isBlank()) ? c : name.trim(),
                coveragePercent, true);
        Database.insuranceNetworks.add(n);
        Database.saveInsuranceNetworks();
        return n;
    }

    public void updateNetwork(String code, String name, Double coveragePercent, Boolean active) {
        InsuranceNetwork n = findNetwork(code);
        if (n == null) throw new IllegalArgumentException("No such network: " + code);
        if (name != null && !name.isBlank()) n.setName(name.trim());
        if (coveragePercent != null) {
            checkCoverage(coveragePercent);
            n.setCoveragePercent(coveragePercent);
        }
        if (active != null) n.setActive(active);
        Database.saveInsuranceNetworks();
    }

    public void removeNetwork(String code) {
        InsuranceNetwork n = findNetwork(code);
        if (n == null) throw new IllegalArgumentException("No such network: " + code);
        Database.insuranceNetworks.remove(n);
        Database.saveInsuranceNetworks();
    }

    private void checkCoverage(double pct) {
        if (pct < 0 || pct > 100)
            throw new IllegalArgumentException("Coverage must be between 0 and 100");
    }
}
