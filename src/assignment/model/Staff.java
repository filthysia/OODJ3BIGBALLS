package assignment.model;

import java.time.LocalDate;

/** A salaried employee of APU Medical Centre attached to a department. */
public abstract class Staff extends Person {

    protected String departmentCode;   // may be null for unassigned staff
    protected LocalDate dateJoined;
    protected double baseSalary;

    protected Staff() { }

    protected Staff(String id, String name, String ic, Gender gender, String phone,
                    String email, String address, String password,
                    String departmentCode, LocalDate dateJoined, double baseSalary) {
        super(id, name, ic, gender, phone, email, address, password);
        this.departmentCode = departmentCode;
        this.dateJoined = dateJoined;
        this.baseSalary = baseSalary;
    }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public LocalDate getDateJoined() { return dateJoined; }
    public void setDateJoined(LocalDate dateJoined) { this.dateJoined = dateJoined; }

    public double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(double baseSalary) { this.baseSalary = baseSalary; }
}
