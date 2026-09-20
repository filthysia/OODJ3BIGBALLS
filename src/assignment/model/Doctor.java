package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;

/** A practising doctor who can be assigned to departments and rostered onto shifts. */
public class Doctor extends Staff {

    private String specialization;
    private boolean available;
    private String managerId;   // Medical Manager this doctor reports to (set by Admin Staff)

    public Doctor() { }

    public Doctor(String id, String name, String ic, Gender gender, String phone,
                  String email, String address, String password, String departmentCode,
                  LocalDate dateJoined, double baseSalary, String specialization, boolean available,
                  String managerId) {
        super(id, name, ic, gender, phone, email, address, password,
                departmentCode, dateJoined, baseSalary);
        this.specialization = specialization;
        this.available = available;
        this.managerId = managerId;
    }

    @Override
    public String getRole() { return "Doctor"; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }

    public String toCsv() {
        return CsvUtil.join(id, name, ic, gender, phone, email, address, password,
                departmentCode, dateJoined, baseSalary, specialization, available, managerId);
    }

    public static Doctor fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Doctor(f[0], f[1], f[2], Gender.fromString(f[3]), f[4], f[5], f[6], f[7],
                blankToNull(f[8]),
                f[9].isBlank() ? null : LocalDate.parse(f[9]),
                f[10].isBlank() ? 0.0 : Double.parseDouble(f[10]),
                f.length > 11 ? f[11] : "",
                f.length > 12 && Boolean.parseBoolean(f[12]),
                (f.length > 13 && !f[13].isBlank()) ? f[13] : null);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Override
    public String toString() {
        return String.format("%-8s %-22s %-14s %-18s %-11s %s",
                id, name, departmentCode == null ? "-" : departmentCode,
                specialization, available ? "available" : "unavailable",
                managerId == null ? "" : "mgr=" + managerId);
    }
}
