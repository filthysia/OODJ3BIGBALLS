package assignment.service;

import assignment.model.Department;
import assignment.model.DepartmentStatus;
import assignment.model.Doctor;

import java.util.List;

/** Use-case: create or update specialised clinical departments (e.g. Cardiology). */
public class DepartmentService {

    public List<Department> getAll() {
        return Database.departments;
    }

    /** @return the department with this code, or {@code null} if none matches. */
    public Department findByCode(String code) {
        if (code == null) return null;
        String key = code.trim();
        for (Department d : Database.departments) {
            if (d.getCode().equalsIgnoreCase(key)) return d;
        }
        return null;
    }

    /** Creates a new department. The code must be unique (case-insensitive). */
    public Department create(String code, String name, String description, double consultationFee) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("Department code is required");
        String normalised = code.trim().toUpperCase();
        if (findByCode(normalised) != null)
            throw new IllegalArgumentException("Department code already exists: " + normalised);
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Department name is required");
        if (consultationFee < 0)
            throw new IllegalArgumentException("Consultation fee cannot be negative");

        Department d = new Department(normalised, name.trim(),
                description == null ? "" : description.trim(),
                null, consultationFee, DepartmentStatus.ACTIVE);
        Database.departments.add(d);
        Database.saveDepartments();
        return d;
    }

    /**
     * Updates an existing department. Pass {@code null} for any field to leave it unchanged.
     */
    public Department update(String code, String name, String description,
                             Double consultationFee, DepartmentStatus status) {
        Department d = requireDept(code);
        if (name != null && !name.isBlank()) d.setName(name.trim());
        if (description != null) d.setDescription(description.trim());
        if (consultationFee != null) {
            if (consultationFee < 0) throw new IllegalArgumentException("Consultation fee cannot be negative");
            d.setConsultationFee(consultationFee);
        }
        if (status != null) d.setStatus(status);
        Database.saveDepartments();
        return d;
    }

    public void assignDoctor(String code, String doctorId) {
        Department d = requireDept(code);
        Doctor doc = requireDoctor(doctorId);
        d.addDoctor(doc.getId());
        doc.setDepartmentCode(d.getCode());
        Database.saveDepartments();
        Database.saveDoctors();
    }

    public void removeDoctor(String code, String doctorId) {
        Department d = requireDept(code);
        d.removeDoctor(doctorId);
        for (Doctor x : Database.doctors) {
            if (x.getId().equals(doctorId) && d.getCode().equalsIgnoreCase(x.getDepartmentCode())) {
                x.setDepartmentCode(null);
            }
        }
        Database.saveDepartments();
        Database.saveDoctors();
    }

    public void setHead(String code, String doctorId) {
        Department d = requireDept(code);
        if (!d.getDoctorIds().contains(doctorId))
            throw new IllegalArgumentException("Doctor must be assigned to the department first");
        d.setHeadDoctorId(doctorId);
        Database.saveDepartments();
    }

    private Department requireDept(String code) {
        Department d = findByCode(code);
        if (d == null) throw new IllegalArgumentException("No such department: " + code);
        return d;
    }

    private Doctor requireDoctor(String doctorId) {
        String key = (doctorId == null) ? "" : doctorId.trim();
        for (Doctor x : Database.doctors) {
            if (x.getId().equalsIgnoreCase(key)) return x;
        }
        throw new IllegalArgumentException("No such doctor: " + doctorId);
    }
}
