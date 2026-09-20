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
        writeCsv("medical_managers.txt", managers.stream().map(MedicalManager::toCsv).toList());
    }

    public static void saveAdminStaff() {
        writeCsv("admin_staff.txt", adminStaff.stream().map(AdminStaff::toCsv).toList());
    }

    public static void saveDoctors() {
        writeCsv("doctors.txt", doctors.stream().map(Doctor::toCsv).toList());
    }

    public static void savePatients() {
        writeCsv("patients.txt", patients.stream().map(Patient::toCsv).toList());
    }

    public static void saveDepartments() {
        writeCsv("departments.txt", departments.stream().map(Department::toCsv).toList());
    }

    public static void saveRostersAndShifts() {
        writeCsv("rosters.txt", rosters.stream().map(ShiftRoster::toCsv).toList());
        writeCsv("shifts.txt", shifts.stream().map(Shift::toCsv).toList());
    }

    public static void saveAssets() {
        writeCsv("assets.txt", assets.stream().map(HospitalAsset::toCsv).toList());
    }

    public static void saveInsuranceNetworks() {
        writeCsv("insurance_networks.txt", insuranceNetworks.stream().map(InsuranceNetwork::toCsv).toList());
    }

    public static void saveClinicConfig() {
        writeCsv("clinic_config.txt", List.of(clinicConfig.toCsv()));
    }

    public static void saveAppointments() {
        writeCsv("appointments.txt", appointments.stream().map(Appointment::toCsv).toList());
    }

    public static void saveInvoices() {
        writeCsv("invoices.txt", invoices.stream().map(Invoice::toCsv).toList());
    }

    public static void saveVitals() {
        writeCsv("vitals.txt", vitals.stream().map(VitalSigns::toCsv).toList());
    }

    public static void saveConsultationNotes() {
        writeCsv("consultation_notes.txt", consultationNotes.stream().map(ConsultationNote::toCsv).toList());
    }

    public static void savePrescriptions() {
        writeCsv("prescriptions.txt", prescriptions.stream().map(Prescription::toCsv).toList());
        writeCsv("prescription_items.txt", prescriptionItems.stream().map(PrescriptionItem::toCsv).toList());
    }

    public static void saveLabRequests() {
        writeCsv("lab_requests.txt", labRequests.stream().map(LabRequest::toCsv).toList());
    }

    public static void saveFeedback() {
        writeCsv("feedback.txt", feedback.stream().map(Feedback::toCsv).toList());
    }

    private static void writeCsv(String file, List<String> lines) {
        FileHandler.writeLines(path(file), new ArrayList<>(lines));
    }
}
