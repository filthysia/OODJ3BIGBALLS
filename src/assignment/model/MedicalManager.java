package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;

/**
 * Domain/account model for the Medical Manager role.
 *
 * <p>Note: this is the data model, distinct from {@code assignment.MedicalManager},
 * which is the Swing form. The form can delegate to {@code assignment.service}
 * classes that operate on this type.</p>
 */
public class MedicalManager extends Staff {

    private String officeLocation;

    public MedicalManager() { }

    public MedicalManager(String id, String name, String ic, Gender gender, String phone,
                          String email, String address, String password, String departmentCode,
                          LocalDate dateJoined, double baseSalary, String officeLocation) {
        super(id, name, ic, gender, phone, email, address, password,
                departmentCode, dateJoined, baseSalary);
        this.officeLocation = officeLocation;
    }

    @Override
    public String getRole() { return "Medical Manager"; }

    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }

    public String toCsv() {
        return CsvUtil.join(id, name, ic, gender, phone, email, address, password,
                departmentCode, dateJoined, baseSalary, officeLocation);
    }

    public static MedicalManager fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new MedicalManager(f[0], f[1], f[2], Gender.fromString(f[3]), f[4], f[5], f[6], f[7],
                f[8].isBlank() ? null : f[8],
                f[9].isBlank() ? null : LocalDate.parse(f[9]),
                f[10].isBlank() ? 0.0 : Double.parseDouble(f[10]),
                f.length > 11 ? f[11] : "");
    }
}
