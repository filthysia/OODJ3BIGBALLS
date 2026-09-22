package assignment.service;

import assignment.model.AdminStaff;
import assignment.model.Appointment;
import assignment.model.ClinicConfig;
import assignment.model.ConsultationNote;
import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Feedback;
import assignment.model.HospitalAsset;
import assignment.model.InsuranceNetwork;
import assignment.model.Invoice;
import assignment.model.LabRequest;
import assignment.model.MedicalManager;
import assignment.model.Patient;
import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.VitalSigns;
import assignment.util.FileHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Central in-memory data store backed by plain-text files under {@link #DATA_DIR}.
 *
 * <p>Call {@link #loadAll()} once at start-up. Each {@code saveX} method rewrites
 * the corresponding file(s) after a change.</p>
 */
public final class Database {

    /** Folder (relative to the working directory) that holds the .txt data files. */
    public static String DATA_DIR = "data";

    // ----- people -----
    public static final List<MedicalManager> managers = new ArrayList<>();
    public static final List<AdminStaff> adminStaff = new ArrayList<>();
    public static final List<Doctor> doctors = new ArrayList<>();
    public static final List<Patient> patients = new ArrayList<>();

    // ----- Medical Manager domain -----
    public static final List<Department> departments = new ArrayList<>();
    public static final List<ShiftRoster> rosters = new ArrayList<>();
    public static final List<Shift> shifts = new ArrayList<>();

    // ----- Admin Staff domain -----
    public static final List<HospitalAsset> assets = new ArrayList<>();
    public static final List<InsuranceNetwork> insuranceNetworks = new ArrayList<>();
    public static ClinicConfig clinicConfig = new ClinicConfig();

    // ----- clinical / front desk -----
    public static final List<Appointment> appointments = new ArrayList<>();
    public static final List<Invoice> invoices = new ArrayList<>();
    public static final List<VitalSigns> vitals = new ArrayList<>();
    public static final List<ConsultationNote> consultationNotes = new ArrayList<>();
    public static final List<Prescription> prescriptions = new ArrayList<>();
    public static final List<PrescriptionItem> prescriptionItems = new ArrayList<>();
    public static final List<LabRequest> labRequests = new ArrayList<>();
    public static final List<Feedback> feedback = new ArrayList<>();

    private Database() { }

    private static String path(String file) {
        return DATA_DIR + "/" + file;
    }

    public static void loadAll() {
        managers.clear();
        adminStaff.clear();
        doctors.clear();
        patients.clear();
        departments.clear();
        rosters.clear();
        shifts.clear();
        assets.clear();
        insuranceNetworks.clear();
        appointments.clear();
        invoices.clear();
        vitals.clear();
        consultationNotes.clear();
        prescriptions.clear();
        prescriptionItems.clear();
        labRequests.clear();
        feedback.clear();

        for (String l : FileHandler.readLines(path("medical_managers.txt"))) {
            if (!l.isBlank()) managers.add(MedicalManager.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("admin_staff.txt"))) {
            if (!l.isBlank()) adminStaff.add(AdminStaff.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("doctors.txt"))) {
            if (!l.isBlank()) doctors.add(Doctor.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("patients.txt"))) {
            if (!l.isBlank()) patients.add(Patient.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("departments.txt"))) {
            if (!l.isBlank()) departments.add(Department.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("rosters.txt"))) {
            if (!l.isBlank()) rosters.add(ShiftRoster.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("shifts.txt"))) {
            if (!l.isBlank()) shifts.add(Shift.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("assets.txt"))) {
            if (!l.isBlank()) assets.add(HospitalAsset.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("insurance_networks.txt"))) {
            if (!l.isBlank()) insuranceNetworks.add(InsuranceNetwork.fromCsv(l));
        }
        List<String> cfg = FileHandler.readLines(path("clinic_config.txt"));
        clinicConfig = cfg.isEmpty() || cfg.get(0).isBlank()
                ? new ClinicConfig() : ClinicConfig.fromCsv(cfg.get(0));
        for (String l : FileHandler.readLines(path("appointments.txt"))) {
            if (!l.isBlank()) appointments.add(Appointment.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("invoices.txt"))) {
            if (!l.isBlank()) invoices.add(Invoice.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("vitals.txt"))) {
            if (!l.isBlank()) vitals.add(VitalSigns.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("consultation_notes.txt"))) {
            if (!l.isBlank()) consultationNotes.add(ConsultationNote.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("prescriptions.txt"))) {
            if (!l.isBlank()) prescriptions.add(Prescription.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("prescription_items.txt"))) {
            if (!l.isBlank()) prescriptionItems.add(PrescriptionItem.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("lab_requests.txt"))) {
            if (!l.isBlank()) labRequests.add(LabRequest.fromCsv(l));
        }
        for (String l : FileHandler.readLines(path("feedback.txt"))) {
            if (!l.isBlank()) feedback.add(Feedback.fromCsv(l));
        }

        // link persisted shifts onto their roster objects
        for (ShiftRoster r : rosters) {
            r.getShifts().clear();
            for (Shift s : shifts) {
                if (s.getRosterId().equals(r.getRosterId())) r.getShifts().add(s);
            }
        }
        // link persisted items onto their prescription objects
        for (Prescription p : prescriptions) {
            p.getItems().clear();
            for (PrescriptionItem it : prescriptionItems) {
                if (it.getPrescriptionId().equals(p.getPrescriptionId())) p.getItems().add(it);
            }
        }
    }

    // ----- save helpers -----

    public static void saveManagers() {
        List<String> lines = new ArrayList<>();
        for (MedicalManager m : managers) lines.add(m.toCsv());
        writeCsv("medical_managers.txt", lines);
    }

    public static void saveAdminStaff() {
        List<String> lines = new ArrayList<>();
        for (AdminStaff a : adminStaff) lines.add(a.toCsv());
        writeCsv("admin_staff.txt", lines);
    }

    public static void saveDoctors() {
        List<String> lines = new ArrayList<>();
        for (Doctor d : doctors) lines.add(d.toCsv());
        writeCsv("doctors.txt", lines);
    }

    public static void savePatients() {
        List<String> lines = new ArrayList<>();
        for (Patient p : patients) lines.add(p.toCsv());
        writeCsv("patients.txt", lines);
    }

    public static void saveDepartments() {
        List<String> lines = new ArrayList<>();
        for (Department d : departments) lines.add(d.toCsv());
        writeCsv("departments.txt", lines);
    }

    public static void saveRostersAndShifts() {
        List<String> rosterLines = new ArrayList<>();
        for (ShiftRoster r : rosters) rosterLines.add(r.toCsv());
        writeCsv("rosters.txt", rosterLines);

        List<String> shiftLines = new ArrayList<>();
        for (Shift s : shifts) shiftLines.add(s.toCsv());
        writeCsv("shifts.txt", shiftLines);
    }

    public static void saveAssets() {
        List<String> lines = new ArrayList<>();
        for (HospitalAsset a : assets) lines.add(a.toCsv());
        writeCsv("assets.txt", lines);
    }

    public static void saveInsuranceNetworks() {
        List<String> lines = new ArrayList<>();
        for (InsuranceNetwork n : insuranceNetworks) lines.add(n.toCsv());
        writeCsv("insurance_networks.txt", lines);
    }

    public static void saveClinicConfig() {
        List<String> lines = new ArrayList<>();
        lines.add(clinicConfig.toCsv());
        writeCsv("clinic_config.txt", lines);
    }

    public static void saveAppointments() {
        List<String> lines = new ArrayList<>();
        for (Appointment a : appointments) lines.add(a.toCsv());
        writeCsv("appointments.txt", lines);
    }

    public static void saveInvoices() {
        List<String> lines = new ArrayList<>();
        for (Invoice i : invoices) lines.add(i.toCsv());
        writeCsv("invoices.txt", lines);
    }

    public static void saveVitals() {
        List<String> lines = new ArrayList<>();
        for (VitalSigns v : vitals) lines.add(v.toCsv());
        writeCsv("vitals.txt", lines);
    }

    public static void saveConsultationNotes() {
        List<String> lines = new ArrayList<>();
        for (ConsultationNote n : consultationNotes) lines.add(n.toCsv());
        writeCsv("consultation_notes.txt", lines);
    }

    public static void savePrescriptions() {
        List<String> prescriptionLines = new ArrayList<>();
        for (Prescription p : prescriptions) prescriptionLines.add(p.toCsv());
        writeCsv("prescriptions.txt", prescriptionLines);

        List<String> itemLines = new ArrayList<>();
        for (PrescriptionItem it : prescriptionItems) itemLines.add(it.toCsv());
        writeCsv("prescription_items.txt", itemLines);
    }

    public static void saveLabRequests() {
        List<String> lines = new ArrayList<>();
        for (LabRequest r : labRequests) lines.add(r.toCsv());
        writeCsv("lab_requests.txt", lines);
    }

    public static void saveFeedback() {
        List<String> lines = new ArrayList<>();
        for (Feedback f : feedback) lines.add(f.toCsv());
        writeCsv("feedback.txt", lines);
    }

    private static void writeCsv(String file, List<String> lines) {
        FileHandler.writeLines(path(file), lines);
    }
}
