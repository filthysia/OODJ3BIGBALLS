package assignment.app;

import assignment.model.DepartmentStatus;
import assignment.model.Gender;
import assignment.model.MedicalManager;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.ShiftStatus;
import assignment.model.ShiftType;
import assignment.service.Database;
import assignment.service.DepartmentService;
import assignment.service.ProfileService;
import assignment.service.ReportService;
import assignment.service.RosterService;
import assignment.service.SeedData;

import java.time.LocalDate;
import java.util.List;

/**
 * Console driver for the Medical Manager module. Wires the service classes
 * together; the Swing form {@code assignment.MedicalManager} can call the same
 * services instead of this menu.
 */
public class MedicalManagerApp {

    private final ProfileService profileService = new ProfileService();
    private final DepartmentService departmentService = new DepartmentService();
    private final RosterService rosterService = new RosterService();
    private final ReportService reportService = new ReportService();

    private MedicalManager current;

    public static void main(String[] args) {
        Database.loadAll();
        SeedData.ensure();
        new MedicalManagerApp().start();
    }

    /** Entry point when launched from {@link HospitalApp} (data already loaded). */
    public void start() {
        System.out.println("\n=== APU Medical Centre :: Medical Manager Module ===");
        if (!login()) {
            System.out.println("Login failed. Exiting.");
            return;
        }

        int choice;
        do {
            System.out.println("\n--- Main Menu (" + current.getName() + ") ---");
            System.out.println("1. Edit my profile");
            System.out.println("2. Manage clinical departments");
            System.out.println("3. Manage doctor shift rosters");
            System.out.println("4. View hospital metrics report");
            System.out.println("5. View revenue summary report");
            System.out.println("0. Logout & exit");
            choice = readInt("Choice: ");
            switch (choice) {
                case 1 -> editProfileMenu();
                case 2 -> departmentMenu();
                case 3 -> rosterMenu();
                case 4 -> System.out.println(reportService.hospitalMetricsReport());
                case 5 -> System.out.println(reportService.revenueSummaryReport());
                case 0 -> System.out.println("Logged out.");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private boolean login() {
        String id = readLine("Manager ID: ");
        String pw = readLine("Password  : ");
        current = profileService.authenticate(id, pw);
        return current != null;
    }

    // ---- 1. Edit personal / individual profile ----
    private void editProfileMenu() {
        System.out.println("\nCurrent profile");
        System.out.println("  Name    : " + current.getName());
        System.out.println("  Phone   : " + current.getPhone());
        System.out.println("  Email   : " + current.getEmail());
        System.out.println("  Address : " + current.getAddress());
        System.out.println("  Gender  : " + current.getGender());
        System.out.println("  Office  : " + current.getOfficeLocation());
        System.out.println("(Leave a field blank to keep the current value.)");

        String name = readLine("New name    : ");
        String phone = readLine("New phone   : ");
        String email = readLine("New email   : ");
        String address = readLine("New address : ");
        String genderStr = readLine("New gender (MALE/FEMALE/OTHER): ");
        String office = readLine("New office  : ");
        Gender gender = genderStr.isBlank() ? null : Gender.fromString(genderStr);

        try {
            profileService.updateProfile(current, name, phone, email, address, gender, office);
            System.out.println("Profile updated.");
        } catch (RuntimeException ex) {
            System.out.println("! " + ex.getMessage());
        }

        if (readLine("Change password? (y/n): ").equalsIgnoreCase("y")) {
            String cur = readLine("Current password        : ");
            String next = readLine("New password (min 6)    : ");
            System.out.println(profileService.changePassword(current, cur, next)
                    ? "Password changed." : "Password change rejected.");
        }
    }

    // ---- 2. Create or update specialised clinical departments ----
    private void departmentMenu() {
        int c;
        do {
            System.out.println("\n--- Departments ---");
            departmentService.getAll().forEach(d -> System.out.println("  " + d));
            System.out.println("1. Create department");
            System.out.println("2. Update department");
            System.out.println("3. Assign doctor to department");
            System.out.println("4. Remove doctor from department");
            System.out.println("5. Set head of department");
            System.out.println("0. Back");
            c = readInt("Choice: ");
            try {
                switch (c) {
                    case 1 -> {
                        String code = readLine("Code (e.g. CARD)      : ");
                        String name = readLine("Name (e.g. Cardiology): ");
                        String desc = readLine("Description           : ");
                        double fee = readDouble("Consultation fee (RM): ");
                        departmentService.create(code, name, desc, fee);
                        System.out.println("Created.");
                    }
                    case 2 -> {
                        String code = readLine("Code to update              : ");
                        String name = readLine("New name (blank=keep)       : ");
                        String desc = readLine("New description (blank=keep): ");
                        String feeStr = readLine("New fee (blank=keep)        : ");
                        String statusStr = readLine("Status ACTIVE/INACTIVE (blank=keep): ");
                        Double fee = feeStr.isBlank() ? null : Double.parseDouble(feeStr);
                        DepartmentStatus st = statusStr.isBlank() ? null : DepartmentStatus.fromString(statusStr);
                        departmentService.update(code,
                                name.isBlank() ? null : name,
                                desc.isBlank() ? null : desc,
                                fee, st);
                        System.out.println("Updated.");
                    }
                    case 3 -> departmentService.assignDoctor(readLine("Dept code: "), readLine("Doctor ID: "));
                    case 4 -> departmentService.removeDoctor(readLine("Dept code: "), readLine("Doctor ID: "));
                    case 5 -> departmentService.setHead(readLine("Dept code: "), readLine("Doctor ID: "));
                    case 0 -> { }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException ex) {
                System.out.println("! " + ex.getMessage());
            }
        } while (c != 0);
    }

    // ---- 3. Design and modify operational shift rosters for doctors ----
    private void rosterMenu() {
        int c;
        do {
            System.out.println("\n--- Shift Rosters ---");
            rosterService.getAll().forEach(r -> System.out.println("  " + r));
            System.out.println("1. Create weekly roster");
            System.out.println("2. Add shift");
            System.out.println("3. Update shift");
            System.out.println("4. Remove shift");
            System.out.println("5. View roster detail");
            System.out.println("0. Back");
            c = readInt("Choice: ");
            try {
                switch (c) {
                    case 1 -> {
                        String dept = readLine("Dept code: ");
                        LocalDate d = LocalDate.parse(readLine("Any date in target week (YYYY-MM-DD): "));
                        ShiftRoster r = rosterService.createRoster(dept, d, current.getId());
                        System.out.println("Created " + r.getRosterId()
                                + " for week " + r.getWeekStart() + ".." + r.getWeekEnd());
                    }
                    case 2 -> {
                        String rid = readLine("Roster ID : ");
                        String doc = readLine("Doctor ID : ");
                        LocalDate d = LocalDate.parse(readLine("Shift date (YYYY-MM-DD): "));
                        ShiftType t = ShiftType.fromString(readLine("Type (MORNING/AFTERNOON/NIGHT): "));
                        Shift s = rosterService.addShift(rid, doc, d, t);
                        System.out.println("Added " + s.getShiftId());
                    }
                    case 3 -> {
                        String sid = readLine("Shift ID: ");
                        String dStr = readLine("New date (blank=keep)  : ");
                        String tStr = readLine("New type (blank=keep)  : ");
                        String stStr = readLine("New status SCHEDULED/COMPLETED/CANCELLED (blank=keep): ");
                        rosterService.updateShift(sid,
                                dStr.isBlank() ? null : LocalDate.parse(dStr),
                                tStr.isBlank() ? null : ShiftType.fromString(tStr),
                                null, null,
                                stStr.isBlank() ? null : ShiftStatus.fromString(stStr));
                        System.out.println("Updated.");
                    }
                    case 4 -> {
                        rosterService.removeShift(readLine("Shift ID: "));
                        System.out.println("Removed.");
                    }
                    case 5 -> {
                        List<Shift> shifts = rosterService.shiftsFor(readLine("Roster ID: "));
                        if (shifts.isEmpty()) System.out.println("(no shifts)");
                        else shifts.forEach(s -> System.out.println("  " + s));
                    }
                    case 0 -> { }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException ex) {
                System.out.println("! " + ex.getMessage());
            }
        } while (c != 0);
    }

    // ---- input helpers (delegate to the shared console reader) ----
    private String readLine(String prompt) {
        return ConsoleIO.line(prompt);
    }

    private int readInt(String prompt) {
        return ConsoleIO.readInt(prompt);
    }

    private double readDouble(String prompt) {
        return ConsoleIO.readDouble(prompt);
    }
}
