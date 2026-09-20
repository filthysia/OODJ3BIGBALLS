package assignment.service;

import assignment.model.AdminStaff;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.MedicalManager;
import assignment.model.Patient;
import assignment.model.Person;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        return allUsers().stream().filter(p -> p.getId().equalsIgnoreCase(key)).findFirst().orElse(null);
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
        if (isSet(departmentCode) && departmentService.findByCode(departmentCode).isEmpty())
            throw new IllegalArgumentException("No such department: " + departmentCode);
        if (isSet(managerId) && Database.managers.stream()
                .noneMatch(m -> m.getId().equalsIgnoreCase(managerId.trim())))
            throw new IllegalArgumentException("No such medical manager: " + managerId);

        String id = IdGenerator.next("DOC", ids(Database.doctors));
        Doctor d = new Doctor(id, req(name, "Name"), ic, gender, phone, email, address, pw(password),
                blank(departmentCode), LocalDate.now(), 0, nz(specialization), true, blank(managerId));
        Database.doctors.add(d);
        Database.saveDoctors();
        if (d.getDepartmentCode() != null) {
            departmentService.findByCode(d.getDepartmentCode()).ifPresent(dep -> {
                dep.addDoctor(d.getId());
                Database.saveDepartments();
            });
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
        switch (p) {
            case AdminStaff a -> {
                Database.adminStaff.remove(a);
                Database.saveAdminStaff();
            }
            case MedicalManager m -> {
                boolean hasDoctors = Database.doctors.stream()
                        .anyMatch(d -> m.getId().equalsIgnoreCase(d.getManagerId()));
                if (hasDoctors)
                    throw new IllegalStateException("Reassign this manager's doctors before deleting");
                Database.managers.remove(m);
                Database.saveManagers();
            }
            case Doctor d -> {
                Database.doctors.remove(d);
                Database.departments.forEach(dep -> dep.removeDoctor(d.getId()));
                Database.saveDoctors();
                Database.saveDepartments();
            }
            case Patient pt -> {
                Database.patients.remove(pt);
                Database.savePatients();
            }
            default -> throw new IllegalStateException("Unknown user type");
        }
    }

    // ---------- assign doctors to their Medical Manager ----------

    public void assignDoctorToManager(String doctorId, String managerId) {
        Doctor d = Database.doctors.stream()
                .filter(x -> x.getId().equalsIgnoreCase(doctorId == null ? "" : doctorId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such doctor: " + doctorId));
        MedicalManager m = Database.managers.stream()
                .filter(x -> x.getId().equalsIgnoreCase(managerId == null ? "" : managerId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such medical manager: " + managerId));
        d.setManagerId(m.getId());
        Database.saveDoctors();
    }

    public List<Doctor> doctorsForManager(String managerId) {
        return Database.doctors.stream()
                .filter(d -> managerId.equalsIgnoreCase(d.getManagerId()))
                .collect(Collectors.toList());
    }

    // ---------- helpers ----------

    private List<String> ids(List<? extends Person> list) {
        return list.stream().map(Person::getId).collect(Collectors.toList());
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
