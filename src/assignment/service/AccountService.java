package assignment.service;

import assignment.model.AdminStaff;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.MedicalManager;
import assignment.model.Patient;
import assignment.model.Person;
import assignment.model.UserRole;

import java.util.List;

/**
 * Shared authentication and "edit my own profile" behaviour for every role.
 * (The Medical Manager module also has its own {@link ProfileService}.)
 */
public class AccountService {

    /** @return the matching person when id + password are correct, otherwise {@code null}. */
    public Person authenticate(UserRole role, String id, String password) {
        Person p;
        switch (role) {
            case ADMIN_STAFF:
                p = find(Database.adminStaff, id);
                break;
            case MEDICAL_MANAGER:
                p = find(Database.managers, id);
                break;
            case DOCTOR:
                p = find(Database.doctors, id);
                break;
            case PATIENT:
                p = find(Database.patients, id);
                break;
            default:
                p = null;
        }
        return (p != null && p.getPassword().equals(password)) ? p : null;
    }

    /** Updates the common profile fields; blank text / null gender leaves a field unchanged. */
    public void editProfile(Person p, String name, String phone, String email,
                            String address, Gender gender) {
        if (p == null) throw new IllegalArgumentException("No account to update");
        if (set(name)) p.setName(name.trim());
        if (set(phone)) p.setPhone(phone.trim());
        if (set(email)) {
            if (!email.contains("@")) throw new IllegalArgumentException("Invalid email address");
            p.setEmail(email.trim());
        }
        if (set(address)) p.setAddress(address.trim());
        if (gender != null) p.setGender(gender);
        persist(p);
    }

    public boolean changePassword(Person p, String current, String next) {
        if (p == null || !p.getPassword().equals(current)) return false;
        if (next == null || next.trim().length() < 6) return false;
        p.setPassword(next.trim());
        persist(p);
        return true;
    }

    /** Rewrites the file that owns this person. */
    public void persist(Person p) {
        if (p instanceof MedicalManager) Database.saveManagers();
        else if (p instanceof AdminStaff) Database.saveAdminStaff();
        else if (p instanceof Doctor) Database.saveDoctors();
        else if (p instanceof Patient) Database.savePatients();
    }

    private <T extends Person> T find(List<T> list, String id) {
        String key = (id == null) ? "" : id.trim();
        for (T x : list) {
            if (x.getId().equalsIgnoreCase(key)) return x;
        }
        return null;
    }

    private boolean set(String s) {
        return s != null && !s.isBlank();
    }
}
