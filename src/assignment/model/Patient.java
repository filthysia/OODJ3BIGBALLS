package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;

/** A registered patient. Included so hospital metric reports can count patients. */
public class Patient extends Person {

    private String bloodType;
    private String allergies;
    private LocalDate registeredOn;

    public Patient() { }

    public Patient(String id, String name, String ic, Gender gender, String phone, String email,
                   String address, String password, String bloodType, String allergies,
                   LocalDate registeredOn) {
        super(id, name, ic, gender, phone, email, address, password);
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.registeredOn = registeredOn;
    }

    @Override
    public String getRole() { return "Patient"; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public LocalDate getRegisteredOn() { return registeredOn; }
    public void setRegisteredOn(LocalDate registeredOn) { this.registeredOn = registeredOn; }

    public String toCsv() {
        return CsvUtil.join(id, name, ic, gender, phone, email, address, password,
                bloodType, allergies, registeredOn);
    }

    public static Patient fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Patient(f[0], f[1], f[2], Gender.fromString(f[3]), f[4], f[5], f[6], f[7],
                f.length > 8 ? f[8] : "",
                f.length > 9 ? f[9] : "",
                (f.length > 10 && !f[10].isBlank()) ? LocalDate.parse(f[10]) : null);
    }
}
