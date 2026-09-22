package assignment.app;

import assignment.model.Appointment;
import assignment.model.ConsultationNote;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.LabRequest;
import assignment.model.LabTestType;
import assignment.model.Patient;
import assignment.model.Person;
import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.model.UserRole;
import assignment.model.VitalSigns;
import assignment.service.AccountService;
import assignment.service.BookingService;
import assignment.service.ConsultationService;
import assignment.service.Database;
import assignment.service.LabRequestService;
import assignment.service.PrescriptionService;
import assignment.service.SeedData;

import java.util.List;

/** Console module for the Doctor role. */
public class DoctorApp {

    private final AccountService accountService = new AccountService();
    private final ConsultationService consultationService = new ConsultationService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final LabRequestService labRequestService = new LabRequestService();
    private final BookingService bookingService = new BookingService();

    private Doctor current;

    public static void main(String[] args) {
        Database.loadAll();
        SeedData.ensure();
        new DoctorApp().start();
    }

    public void start() {
        System.out.println("\n=== APU Medical Centre :: Doctor Module ===");
        String id = ConsoleIO.line("Doctor ID : ");
        String pw = ConsoleIO.line("Password  : ");
        Person p = accountService.authenticate(UserRole.DOCTOR, id, pw);
        if (!(p instanceof Doctor)) {
            System.out.println("Login failed.");
            return;
        }
        current = (Doctor) p;

        int choice;
        do {
            System.out.println("\n--- Doctor Menu (" + current.getName() + ") ---");
            System.out.println("1. Edit my profile");
            System.out.println("2. My appointments");
            System.out.println("3. Log vital signs / write consultation note");
            System.out.println("4. Issue a prescription");
            System.out.println("5. Request lab test / X-ray / imaging");
            System.out.println("0. Logout");
            choice = ConsoleIO.readInt("Choice: ");
            try {
                switch (choice) {
                    case 1:
                        profileMenu();
                        break;
                    case 2:
                        appointmentMenu();
                        break;
                    case 3:
                        clinicalMenu();
                        break;
                    case 4:
                        prescriptionMenu();
                        break;
                    case 5:
                        labRequestMenu();
                        break;
                    case 0:
                        System.out.println("Logged out.");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (RuntimeException ex) {
                System.out.println("! " + ex.getMessage());
            }
        } while (choice != 0);
    }

    // ---------- 1. profile ----------
    private void profileMenu() {
        System.out.println("\nName        : " + current.getName());
        System.out.println("Phone       : " + current.getPhone());
        System.out.println("Email       : " + current.getEmail());
        System.out.println("Address     : " + current.getAddress());
        System.out.println("Specialty   : " + current.getSpecialization());
        System.out.println("Available   : " + current.isAvailable());
        System.out.println("(Leave blank to keep a value.)");

        String name = ConsoleIO.line("New name    : ");
        String phone = ConsoleIO.line("New phone   : ");
        String email = ConsoleIO.line("New email   : ");
        String address = ConsoleIO.line("New address : ");
        String genderStr = ConsoleIO.line("New gender (MALE/FEMALE): ");
        Gender gender = genderStr.isBlank() ? null : Gender.fromString(genderStr);
        accountService.editProfile(current, name, phone, email, address, gender);

        String spec = ConsoleIO.line("New specialization: ");
        if (!spec.isBlank()) current.setSpecialization(spec.trim());
        String avail = ConsoleIO.line("Available for new bookings? (y/n, blank=keep): ");
        if (!avail.isBlank()) current.setAvailable(avail.equalsIgnoreCase("y"));
        accountService.persist(current);
        System.out.println("Profile saved.");

        if (ConsoleIO.confirm("Change password?")) {
            String cur = ConsoleIO.line("Current password: ");
            String next = ConsoleIO.line("New password (min 6): ");
            System.out.println(accountService.changePassword(current, cur, next)
                    ? "Password changed." : "Rejected.");
        }
    }

    // ---------- 2. appointments ----------
    private void appointmentMenu() {
        int c;
        do {
            System.out.println("\n--- My Appointments ---");
            List<Appointment> appts = bookingService.forDoctor(current.getId());
            if (appts.isEmpty()) System.out.println("  (none)");
            for (Appointment a : appts) System.out.println("  " + a);
            System.out.println("1. Mark completed   2. Mark no-show   0. Back");
            c = ConsoleIO.readInt("Choice: ");
            try {
                switch (c) {
                    case 1:
                        bookingService.markCompleted(ConsoleIO.line("Appointment ID: "));
                        System.out.println("Marked completed.");
                        break;
                    case 2:
                        bookingService.markNoShow(ConsoleIO.line("Appointment ID: "));
                        System.out.println("Marked no-show.");
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (RuntimeException ex) {
                System.out.println("! " + ex.getMessage());
            }
        } while (c != 0);
    }

    // ---------- 3. vitals + notes ----------
    private void clinicalMenu() {
        String patientId = ConsoleIO.line("Patient ID: ");
        String key = patientId.trim();
        Patient patient = null;
        for (Patient x : Database.patients) {
            if (x.getId().equalsIgnoreCase(key)) {
                patient = x;
                break;
            }
        }
        if (patient == null) throw new IllegalArgumentException("No such patient: " + patientId);
        System.out.println("Patient: " + patient.getName()
                + "  blood=" + patient.getBloodType() + "  allergies=" + patient.getAllergies());

        String appointmentId = ConsoleIO.line("Related appointment ID (blank=none): ");

        if (ConsoleIO.confirm("Log vital signs now?")) {
            double temp = ConsoleIO.readDouble("Temperature (C)   : ");
            int sys = ConsoleIO.readInt("BP systolic       : ");
            int dia = ConsoleIO.readInt("BP diastolic      : ");
            int hr = ConsoleIO.readInt("Heart rate        : ");
            int rr = ConsoleIO.readInt("Respiratory rate  : ");
            int spo2 = ConsoleIO.readInt("SpO2 %            : ");
            double weight = ConsoleIO.readDouble("Weight (kg)       : ");
            double height = ConsoleIO.readDouble("Height (cm)       : ");
            VitalSigns v = consultationService.logVitals(patient.getId(), current.getId(), appointmentId,
                    temp, sys, dia, hr, rr, spo2, weight, height);
            System.out.printf("Recorded %s (BMI %.1f)%n", v.getRecordId(), v.bmi());
        }

        if (ConsoleIO.confirm("Write a consultation note now?")) {
            String symptoms = ConsoleIO.line("Symptoms  : ");
            String diagnosis = ConsoleIO.line("Diagnosis : ");
            String notes = ConsoleIO.line("Notes     : ");
            ConsultationNote n = consultationService.writeNote(appointmentId, patient.getId(),
                    current.getId(), symptoms, diagnosis, notes);
            System.out.println("Saved note " + n.getNoteId());
        }
    }

    // ---------- 4. prescriptions ----------
    private void prescriptionMenu() {
        String patientId = ConsoleIO.line("Patient ID: ");
        String appointmentId = ConsoleIO.line("Related appointment ID (blank=none): ");
        Prescription rx = prescriptionService.issue(patientId, current.getId(), appointmentId);
        System.out.println("Started prescription " + rx.getPrescriptionId());

        while (ConsoleIO.confirm("Add a medication line?")) {
            String drug = ConsoleIO.required("Drug name    : ");
            String dose = ConsoleIO.line("Dosage       : ");
            String freq = ConsoleIO.line("Frequency    : ");
            int days = ConsoleIO.readInt("Duration days: ");
            int qty = ConsoleIO.readInt("Quantity     : ");
            String instr = ConsoleIO.line("Instructions : ");
            PrescriptionItem item = prescriptionService.addItem(rx.getPrescriptionId(),
                    drug, dose, freq, days, qty, instr);
            System.out.println("  added " + item.getItemId());
        }
        if (rx.getItems().isEmpty()) {
            prescriptionService.cancel(rx.getPrescriptionId());
            System.out.println("No items added - prescription cancelled.");
        } else {
            System.out.println("Prescription " + rx.getPrescriptionId()
                    + " has " + rx.getItems().size() + " item(s).");
        }
    }

    // ---------- 5. lab / imaging requests ----------
    private void labRequestMenu() {
        String patientId = ConsoleIO.line("Patient ID: ");
        String appointmentId = ConsoleIO.line("Related appointment ID (blank=none): ");
        System.out.println("Test types: BLOOD_TEST, URINE_TEST, XRAY, CT_SCAN, MRI, ULTRASOUND");
        LabTestType type = LabTestType.fromString(ConsoleIO.line("Type: "));
        String reason = ConsoleIO.line("Clinical reason: ");
        LabRequest r = labRequestService.raise(patientId, current.getId(), appointmentId, type, reason);
        System.out.println("Raised " + r.getRequestId() + " (" + r.getStatus()
                + ") - Admin Staff will schedule it onto a " + type.getRequiredAsset() + ".");
    }
}
