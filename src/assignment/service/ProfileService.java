package assignment.service;

import assignment.model.Gender;
import assignment.model.MedicalManager;

/** Use-case: the Medical Manager edits their own individual profile. */
public class ProfileService {

    public MedicalManager findById(String id) {
        for (MedicalManager m : Database.managers) {
            if (m.getId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    /** Returns the manager when id + password match, otherwise {@code null}. */
    public MedicalManager authenticate(String id, String password) {
        MedicalManager m = findById(id);
        return (m != null && m.getPassword().equals(password)) ? m : null;
    }

    /**
     * Updates the editable profile fields. A {@code null} or blank text value
     * leaves the existing value untouched; a {@code null} gender is ignored.
     */
    public void updateProfile(MedicalManager m, String name, String phone, String email,
                              String address, Gender gender, String officeLocation) {
        if (m == null) throw new IllegalArgumentException("No manager to update");
        if (isSet(name)) m.setName(name.trim());
        if (isSet(phone)) m.setPhone(phone.trim());
        if (isSet(email)) {
            if (!email.contains("@")) throw new IllegalArgumentException("Invalid email address");
            m.setEmail(email.trim());
        }
        if (isSet(address)) m.setAddress(address.trim());
        if (gender != null) m.setGender(gender);
        if (isSet(officeLocation)) m.setOfficeLocation(officeLocation.trim());
        Database.saveManagers();
    }

    /** @return true on success; false if the current password is wrong or the new one is too weak. */
    public boolean changePassword(MedicalManager m, String current, String next) {
        if (m == null || !m.getPassword().equals(current)) return false;
        if (next == null || next.trim().length() < 6) return false;
        m.setPassword(next.trim());
        Database.saveManagers();
        return true;
    }

    private boolean isSet(String s) {
        return s != null && !s.isBlank();
    }
}
