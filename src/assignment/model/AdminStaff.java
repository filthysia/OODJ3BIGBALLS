package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;

/** An administrative staff member who manages users, assets and billing configuration. */
public class AdminStaff extends Staff {

    private String deskLocation;

    public AdminStaff() { }

    public AdminStaff(String id, String name, String ic, Gender gender, String phone,
                      String email, String address, String password, String departmentCode,
                      LocalDate dateJoined, double baseSalary, String deskLocation) {
        super(id, name, ic, gender, phone, email, address, password,
                departmentCode, dateJoined, baseSalary);
        this.deskLocation = deskLocation;
    }

    @Override
    public String getRole() { return "Admin Staff"; }

    public String getDeskLocation() { return deskLocation; }
    public void setDeskLocation(String deskLocation) { this.deskLocation = deskLocation; }

    public String toCsv() {
        return CsvUtil.join(id, name, ic, gender, phone, email, address, password,
                departmentCode, dateJoined, baseSalary, deskLocation);
    }

    public static AdminStaff fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new AdminStaff(f[0], f[1], f[2], Gender.fromString(f[3]), f[4], f[5], f[6], f[7],
                f[8].isBlank() ? null : f[8],
                f[9].isBlank() ? null : LocalDate.parse(f[9]),
                f[10].isBlank() ? 0.0 : Double.parseDouble(f[10]),
                f.length > 11 ? f[11] : "");
    }
}
