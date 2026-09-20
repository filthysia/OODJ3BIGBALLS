package assignment.app;

import assignment.model.Appointment;
import assignment.model.Doctor;
import assignment.model.Feedback;
import assignment.model.Gender;
import assignment.model.Patient;
import assignment.model.Person;
import assignment.model.Slot;
import assignment.model.UserRole;
import assignment.service.AccountService;
import assignment.service.BookingService;
import assignment.service.Database;
import assignment.service.FeedbackService;
import assignment.service.MedicalHistoryService;
import assignment.service.PrescriptionService;
import assignment.service.SeedData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Console module for the Patient role. */
public class PatientApp {

    private final AccountService accountService = new AccountService();
    private final BookingService bookingService = new BookingService();
    private final MedicalHistoryService historyService = new MedicalHistoryService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final FeedbackService feedbackService = new FeedbackService();

    private Patient current;

    public static void main(String[] args) {
        Database.loadAll();
        SeedData.ensure();
        new PatientApp().start();
    }

    public void start() {
        System.out.println("\n=== APU Medical Centre :: Patient Portal ===");
        String id = ConsoleIO.line("Patient ID : ");
        String pw = ConsoleIO.line("Password   : ");
        Person p = accountService.authenticate(UserRole.PATIENT, id, pw);
        if (!(p instanceof Patient patient)) {
            System.out.println("Login failed.");
            return;
        }
        current = patient;

        int choice;
        do {
            System.out.println("\n--- Patient Menu (" + current.getName() + ") ---");
            System.out.println("1. Edit my profile");
            System.out.println("2. Book / reschedule / cancel an appointment");
            System.out.println("3. View my medical history & prescriptions");
            System.out.println("4. Rate a doctor / visit");
            System.out.println("0. Logout");
            choice = ConsoleIO.readInt("Choice: ");
            try {
                switch (choice) {
                    case 1 -> profileMenu();
                    case 2 -> bookingMenu();
                    case 3 -> historyMenu();
                    case 4 -> feedbackMenu();
                    case 0 -> System.out.println("Logged out.");
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException ex) {
                System.out.println("! " + ex.getMessage());
            }
        } while (choice != 0);
    }

    // ---------- 1. profile ----------
    private void profileMenu() {
        System.out.println("\nName      : " + current.getName());
        System.out.println("Phone     : " + current.getPhone());
        System.out.println("Email     : " + current.getEmail());
        System.out.println("Address   : " + current.getAddress());
        System.out.println("Blood type: " + current.getBloodType());
        System.out.println("Allergies : " + current.getAllergies());
        System.out.println("(Leave blank to keep a value.)");

        String name = ConsoleIO.line("New name    : ");
        String phone = ConsoleIO.line("New phone   : ");
        String email = ConsoleIO.line("New email   : ");
        String address = ConsoleIO.line("New address : ");
        String genderStr = ConsoleIO.line("New gender (MALE/FEMALE/OTHER): ");
        Gender gender = genderStr.isBlank() ? null : Gender.fromString(genderStr);
        accountService.editProfile(current, name, phone, email, address, gender);

        String blood = ConsoleIO.line("New blood type: ");
        if (!blood.isBlank()) current.setBloodType(blood.trim());
        String allergies = ConsoleIO.line("New allergies : ");
        if (!allergies.isBlank()) current.setAllergies(allergies.trim());
        accountService.persist(current);
        System.out.println("Profile saved.");

        if (ConsoleIO.confirm("Change password?")) {
            String cur = ConsoleIO.line("Current password: ");
            String next = ConsoleIO.line("New password (min 6): ");
            System.out.println(accountService.changePassword(current, cur, next)
                    ? "Password changed." : "Rejected.");
        }
    }

    // ---------- 2. booking ----------
    private void bookingMenu() {
        int c;
        do {
            System.out.println("\n--- My Appointments ---");
            List<Appointment> mine = bookingService.forPatient(current.getId());
            if (mine.isEmpty()) System.out.println("  (none)");
            for (Appointment a : mine) {
                System.out.printf("  %-9s %s  Dr %-8s %-6s %s%n",
                        a.getAppointmentId(), a.getDateTime(), a.getDoctorId(),
                        a.getDepartmentCode(), a.getStatus());
            }
            System.out.println("1. Browse doctor slots & book   2. Reschedule   3. Cancel   0. Back");
            c = ConsoleIO.readInt("Choice: ");
            try {
                switch (c) {
                    case 1 -> browseAndBook();
                    case 2 -> {
                        String aid = ConsoleIO.line("Appointment ID: ");
                        LocalDate d = ConsoleIO.readDate("New date (YYYY-MM-DD): ");
                        LocalTime t = ConsoleIO.readTime("New start time (HH:MM): ");
                        Appointment a = bookingService.reschedule(aid, d, t);
                        System.out.println("Moved to " + a.getDateTime());
                    }
                    case 3 -> {
                        bookingService.cancel(ConsoleIO.line("Appointment ID: "));
                        System.out.println("Cancelled.");
                    }
                    case 0 -> { }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException ex) {
                System.out.println("! " + ex.getMessage());
            }
        } while (c != 0);
    }

    private void browseAndBook() {
        System.out.println("Doctors:");
        for (Doctor d : Database.doctors) {
            System.out.printf("  %-8s %-22s %-14s %s%n", d.getId(), d.getName(),
                    d.getDepartmentCode(), d.isAvailable() ? "" : "(not taking bookings)");
        }
        String doctorId = ConsoleIO.line("Doctor ID: ");
        LocalDate date = ConsoleIO.readDate("Date (YYYY-MM-DD): ");
        List<Slot> slots = bookingService.availableSlots(doctorId, date);
        if (slots.isEmpty()) {
            System.out.println("No open slots that day (the doctor may not be rostered).");
            return;
        }
        System.out.println("Open slots:");
        for (Slot s : slots) System.out.println("  " + s.getStart() + " - " + s.getEnd());
        LocalTime start = ConsoleIO.readTime("Start time to book (HH:MM): ");
        Appointment appt = bookingService.book(current.getId(), doctorId, date, start);
        System.out.printf("Booked %s on %s, fee RM%.2f%n",
                appt.getAppointmentId(), appt.getDateTime(), appt.getConsultationFee());
    }

    // ---------- 3. history ----------
    private void historyMenu() {
        System.out.println();
        System.out.println(historyService.historyReport(current.getId()));
        if (ConsoleIO.confirm("Show full prescription detail?")) {
            prescriptionService.forPatient(current.getId()).forEach(rx -> {
                System.out.println(rx);
                rx.getItems().forEach(it -> System.out.println("     " + it));
            });
        }
    }

    // ---------- 4. feedback ----------
    private void feedbackMenu() {
        System.out.println("\nYour completed visits:");
        bookingService.forPatient(current.getId()).stream()
                .filter(a -> a.getStatus().name().equals("COMPLETED"))
                .forEach(a -> System.out.printf("  %-9s %s  Dr %s%n",
                        a.getAppointmentId(), a.getDateTime(), a.getDoctorId()));

        String appointmentId = ConsoleIO.line("Appointment ID (blank to rate a doctor directly): ");
        String doctorId = appointmentId.isBlank() ? ConsoleIO.line("Doctor ID: ") : null;
        int docRating = ConsoleIO.readInt("Doctor rating (1-5) : ");
        int visitRating = ConsoleIO.readInt("Visit rating  (1-5) : ");
        String comment = ConsoleIO.line("Comment: ");
        Feedback fb = feedbackService.submit(current.getId(), doctorId,
                appointmentId.isBlank() ? null : appointmentId, docRating, visitRating, comment);
        System.out.println("Thank you - feedback " + fb.getFeedbackId() + " recorded.");
    }
}
