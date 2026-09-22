package assignment.service;

import assignment.model.AdminStaff;
import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.MedicalManager;
import assignment.model.Patient;
import assignment.model.Person;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Use-case: Admin Staff creates, reads, updates and deletes end users. */
public class UserService {

    private final DepartmentService departmentService = new DepartmentService();
    private final AccountService accountService = new AccountService();

    public List<Person> allUsers() {
        List<Person> all = new ArrayList<>();
        all.addAll(Database.adminStaff);
        all.addAll(Database.managers);
        all.addAll(Database.doctors);
        all.addAll(Database.patients);
        return all;
    }

    public Person find(String id) {
        String key = (id == null) ? "" : id.trim();
        for (Person p : allUsers()) {
            if (p.getId().equalsIgnoreCase(key)) return p;
        }
        return null;
    }

    public Person requireUser(String id) {
        Person p = find(id);
        if (p == null) throw new IllegalArgumentException("No such user: " + id);
        return p;
    }

    // ---------- create ----------

    public AdminStaff createAdminStaff(String name, String ic, Gender gender, String phone,
                                       String email, String address, String password, String deskLocation) {
        String id = IdGenerator.next("AS", ids(Database.adminStaff));
        AdminStaff a = new AdminStaff(id, req(name, "Name"), ic, gender, phone, email, address,
                pw(password), null, LocalDate.now(), 0, nz(deskLocation));
        Database.adminStaff.add(a);
        Database.saveAdminStaff();
        return a;
    }

    public MedicalManager createMedicalManager(String name, String ic, Gender gender, String phone,
                                               String email, String address, String password, String office) {
        String id = IdGenerator.next("MM", ids(Database.managers));
        MedicalManager m = new MedicalManager(id, req(name, "Name"), ic, gender, phone, email, address,
                pw(password), null, LocalDate.now(), 0, nz(office));
        Database.managers.add(m);
        Database.saveManagers();
        return m;
    }

    public Doctor createDoctor(String name, String ic, Gender gender, String phone, String email,
                               String address, String password, String departmentCode,
                               String specialization, String managerId) {
        if (isSet(departmentCode) && departmentService.findByCode(departmentCode) == null)
            throw new IllegalArgumentException("No such department: " + departmentCode);
        if (isSet(managerId) && !managerExists(managerId))
            throw new IllegalArgumentException("No such medical manager: " + managerId);

        String id = IdGenerator.next("DOC", ids(Database.doctors));
        Doctor d = new Doctor(id, req(name, "Name"), ic, gender, phone, email, address, pw(password),
                blank(departmentCode), LocalDate.now(), 0, nz(specialization), true, blank(managerId));
        Database.doctors.add(d);
        Database.saveDoctors();
        if (d.getDepartmentCode() != null) {
            Department dep = departmentService.findByCode(d.getDepartmentCode());
            if (dep != null) {
                dep.addDoctor(d.getId());
                Database.saveDepartments();
            }
        }
        return d;
    }

    public Patient createPatient(String name, String ic, Gender gender, String phone, String email,
                                 String address, String password, String bloodType, String allergies) {
        String id = IdGenerator.next("PT", ids(Database.patients));
        Patient p = new Patient(id, req(name, "Name"), ic, gender, phone, email, address, pw(password),
                nz(bloodType), nz(allergies), LocalDate.now());
        Database.patients.add(p);
        Database.savePatients();
        return p;
    }

    // ---------- update / delete ----------

    public void resetPassword(String id, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        Person p = requireUser(id);
        p.setPassword(newPassword.trim());
        accountService.persist(p);
    }

    public void updateContact(String id, String phone, String email, String address) {
        Person p = requireUser(id);
        accountService.editProfile(p, null, phone, email, address, null);
    }

    public void deleteUser(String id) {
        Person p = requireUser(id);
        if (p instanceof AdminStaff) {
            AdminStaff a = (AdminStaff) p;
            Database.adminStaff.remove(a);
            Database.saveAdminStaff();
        } else if (p instanceof MedicalManager) {
            MedicalManager m = (MedicalManager) p;
            boolean hasDoctors = false;
            for (Doctor d : Database.doctors) {
                if (m.getId().equalsIgnoreCase(d.getManagerId())) {
                    hasDoctors = true;
                    break;
                }
            }
            if (hasDoctors)
                throw new IllegalStateException("Reassign this manager's doctors before deleting");
            Database.managers.remove(m);
            Database.saveManagers();
        } else if (p instanceof Doctor) {
            Doctor d = (Doctor) p;
            Database.doctors.remove(d);
            for (Department dep : Database.departments) {
                dep.removeDoctor(d.getId());
            }
            Database.saveDoctors();
            Database.saveDepartments();
        } else if (p instanceof Patient) {
            Patient pt = (Patient) p;
            Database.patients.remove(pt);
            Database.savePatients();
        } else {
            throw new IllegalStateException("Unknown user type");
        }
    }

    // ---------- assign doctors to their Medical Manager ----------

    public void assignDoctorToManager(String doctorId, String managerId) {
        String doctorKey = (doctorId == null) ? "" : doctorId.trim();
        Doctor d = null;
        for (Doctor x : Database.doctors) {
            if (x.getId().equalsIgnoreCase(doctorKey)) {
                d = x;
                break;
            }
        }
        if (d == null) throw new IllegalArgumentException("No such doctor: " + doctorId);

        String managerKey = (managerId == null) ? "" : managerId.trim();
        MedicalManager m = null;
        for (MedicalManager x : Database.managers) {
            if (x.getId().equalsIgnoreCase(managerKey)) {
                m = x;
                break;
            }
        }
        if (m == null) throw new IllegalArgumentException("No such medical manager: " + managerId);

        d.setManagerId(m.getId());
        Database.saveDoctors();
    }

    public List<Doctor> doctorsForManager(String managerId) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor d : Database.doctors) {
            if (managerId.equalsIgnoreCase(d.getManagerId())) result.add(d);
        }
        return result;
    }

    // ---------- helpers ----------

    private boolean managerExists(String managerId) {
        for (MedicalManager m : Database.managers) {
            if (m.getId().equalsIgnoreCase(managerId.trim())) return true;
        }
        return false;
    }

    private List<String> ids(List<? extends Person> list) {
        List<String> result = new ArrayList<>();
        for (Person p : list) result.add(p.getId());
        return result;
    }

    private static boolean isSet(String s) { return s != null && !s.isBlank(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String pw(String p) { return (p == null || p.isBlank()) ? "changeme" : p.trim(); }

    private static String req(String s, String label) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(label + " is required");
        return s.trim();
    }
}
