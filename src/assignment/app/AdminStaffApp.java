package assignment.app;

import assignment.model.AdminStaff;
import assignment.model.AssetStatus;
import assignment.model.AssetType;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.HospitalAsset;
import assignment.model.InsuranceNetwork;
import assignment.model.LabRequest;
import assignment.model.MedicalManager;
import assignment.model.Patient;
import assignment.model.Person;
import assignment.model.UserRole;
import assignment.service.AccountService;
import assignment.service.AssetService;
import assignment.service.BillingConfigService;
import assignment.service.Database;
import assignment.service.LabRequestService;
import assignment.service.SeedData;
import assignment.service.UserService;

/** Console module for the Admin Staff role. */
public class AdminStaffApp {

    private final AccountService accountService = new AccountService();
    private final UserService userService = new UserService();
    private final AssetService assetService = new AssetService();
    private final BillingConfigService billingConfigService = new BillingConfigService();
    private final LabRequestService labRequestService = new LabRequestService();

    private AdminStaff current;

    public static void main(String[] args) {
        Database.loadAll();
        SeedData.ensure();
        new AdminStaffApp().start();
    }

    /** Entry point when launched from {@link HospitalApp} (data already loaded). */
    public void start() {
        System.out.println("\n=== APU Medical Centre :: Admin Staff Module ===");
        String id = ConsoleIO.line("Staff ID : ");
        String pw = ConsoleIO.line("Password : ");
        Person p = accountService.authenticate(UserRole.ADMIN_STAFF, id, pw);
        if (!(p instanceof AdminStaff)) {
            System.out.println("Login failed.");
            return;
        }
        current = (AdminStaff) p;

        int choice;
        do {
            System.out.println("\n--- Admin Staff Menu (" + current.getName() + ") ---");
            System.out.println("1. Manage end users (CRUD)");
            System.out.println("2. Assign doctors to Medical Managers");
            System.out.println("3. Manage & allocate hospital assets");
            System.out.println("4. Configure consultation rates & insurance networks");
            System.out.println("5. Handle lab / imaging requests");
            System.out.println("6. Edit my profile");
            System.out.println("0. Logout");
            choice = ConsoleIO.readInt("Choice: ");
            try {
                switch (choice) {
                    case 1:
                        userMenu();
                        break;
                    case 2:
                        assignDoctorMenu();
                        break;
                    case 3:
                        assetMenu();
                        break;
                    case 4:
                        billingMenu();
                        break;
                    case 5:
                        labMenu();
                        break;
                    case 6:
                        profileMenu();
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

    // ---------- 1. end users ----------
    private void userMenu() {
        int c;
        do {
            System.out.println("\n--- End Users ---");
            for (Person p : userService.allUsers()) System.out.println("  " + p);
            System.out.println("1. Create user   2. View user   3. Update contact");
            System.out.println("4. Reset password   5. Delete user   0. Back");
            c = ConsoleIO.readInt("Choice: ");
            try {
                switch (c) {
                    case 1:
                        createUser();
                        break;
                    case 2: {
                        Person p = userService.requireUser(ConsoleIO.line("User ID: "));
                        printUser(p);
                        break;
                    }
                    case 3: {
                        String id = ConsoleIO.line("User ID: ");
                        String phone = ConsoleIO.line("New phone (blank=keep)  : ");
                        String email = ConsoleIO.line("New email (blank=keep)  : ");
                        String address = ConsoleIO.line("New address (blank=keep): ");
                        userService.updateContact(id, phone, email, address);
                        System.out.println("Updated.");
                        break;
                    }
                    case 4: {
                        String id = ConsoleIO.line("User ID: ");
                        String pw = ConsoleIO.line("New password (min 6): ");
                        userService.resetPassword(id, pw);
                        System.out.println("Password reset.");
                        break;
                    }
                    case 5: {
                        String id = ConsoleIO.line("User ID: ");
                        if (ConsoleIO.confirm("Delete " + id + "?")) {
                            userService.deleteUser(id);
                            System.out.println("Deleted.");
                        }
                        break;
                    }
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

    private void createUser() {
        System.out.println("Type: 1=Admin Staff  2=Medical Manager  3=Doctor  4=Patient");
        int t = ConsoleIO.readInt("Choice: ");
        String name = ConsoleIO.required("Full name : ");
        String ic = ConsoleIO.line("IC / passport : ");
        Gender gender = Gender.fromString(ConsoleIO.line("Gender (MALE/FEMALE/OTHER): "));
        String phone = ConsoleIO.line("Phone   : ");
        String email = ConsoleIO.line("Email   : ");
        String address = ConsoleIO.line("Address : ");
        String password = ConsoleIO.line("Initial password (min 6): ");

        switch (t) {
            case 1: {
                String desk = ConsoleIO.line("Desk location: ");
                System.out.println("Created " + userService.createAdminStaff(
                        name, ic, gender, phone, email, address, password, desk).getId());
                break;
            }
            case 2: {
                String office = ConsoleIO.line("Office location: ");
                System.out.println("Created " + userService.createMedicalManager(
                        name, ic, gender, phone, email, address, password, office).getId());
                break;
            }
            case 3: {
                String dept = ConsoleIO.line("Department code (blank=none): ");
                String spec = ConsoleIO.line("Specialization: ");
                String mgr = ConsoleIO.line("Medical Manager ID (blank=none): ");
                System.out.println("Created " + userService.createDoctor(
                        name, ic, gender, phone, email, address, password, dept, spec, mgr).getId());
                break;
            }
            case 4: {
                String blood = ConsoleIO.line("Blood type: ");
                String allergies = ConsoleIO.line("Allergies: ");
                System.out.println("Created " + userService.createPatient(
                        name, ic, gender, phone, email, address, password, blood, allergies).getId());
                break;
            }
            default:
                System.out.println("Unknown type.");
        }
    }

    private void printUser(Person p) {
        System.out.println("  ID       : " + p.getId());
        System.out.println("  Name     : " + p.getName());
        System.out.println("  Role     : " + p.getRole());
        System.out.println("  Gender   : " + p.getGender());
        System.out.println("  Phone    : " + p.getPhone());
        System.out.println("  Email    : " + p.getEmail());
        System.out.println("  Address  : " + p.getAddress());
        if (p instanceof Doctor) {
            Doctor d = (Doctor) p;
            System.out.println("  Dept     : " + d.getDepartmentCode());
            System.out.println("  Special. : " + d.getSpecialization());
            System.out.println("  Manager  : " + d.getManagerId());
        } else if (p instanceof Patient) {
            Patient pt = (Patient) p;
            System.out.println("  Blood    : " + pt.getBloodType());
            System.out.println("  Allergies: " + pt.getAllergies());
        }
    }

    // ---------- 2. assign doctors to managers ----------
    private void assignDoctorMenu() {
        System.out.println("\n--- Doctors and their Medical Managers ---");
        for (Doctor d : Database.doctors) {
            System.out.printf("  %-8s %-22s dept=%-6s manager=%s%n",
                    d.getId(), d.getName(), d.getDepartmentCode(),
                    d.getManagerId() == null ? "(none)" : d.getManagerId());
        }
        System.out.println("Medical Managers:");
        for (MedicalManager m : Database.managers) {
            System.out.printf("  %-8s %s%n", m.getId(), m.getName());
        }
        String doc = ConsoleIO.line("Doctor ID  : ");
        String mgr = ConsoleIO.line("Manager ID : ");
        userService.assignDoctorToManager(doc, mgr);
        System.out.println("Assigned.");
    }

    // ---------- 3. assets ----------
    private void assetMenu() {
        int c;
        do {
            System.out.println("\n--- Hospital Assets ---");
            for (HospitalAsset a : assetService.getAll()) System.out.println("  " + a);
            System.out.println("1. Add asset   2. Update asset   3. Allocate to department");
            System.out.println("4. Release   5. Delete   0. Back");
            c = ConsoleIO.readInt("Choice: ");
            try {
                switch (c) {
                    case 1: {
                        String name = ConsoleIO.required("Name: ");
                        AssetType type = AssetType.fromString(ConsoleIO.line(
                                "Type (CONSULTATION_ROOM/INPATIENT_WARD/LAB/IMAGING_ROOM): "));
                        String loc = ConsoleIO.line("Location: ");
                        int cap = ConsoleIO.readInt("Capacity: ");
                        System.out.println("Added " + assetService.create(name, type, loc, cap).getAssetId());
                        break;
                    }
                    case 2: {
                        String id = ConsoleIO.line("Asset ID: ");
                        String name = ConsoleIO.line("New name (blank=keep)    : ");
                        String loc = ConsoleIO.line("New location (blank=keep): ");
                        Integer cap = ConsoleIO.readIntOrNull("New capacity (blank=keep): ");
                        String st = ConsoleIO.line("New status AVAILABLE/ALLOCATED/MAINTENANCE/RETIRED (blank=keep): ");
                        AssetStatus status = st.isBlank() ? null : AssetStatus.fromString(st);
                        assetService.update(id, name.isBlank() ? null : name,
                                loc.isBlank() ? null : loc, cap, status);
                        System.out.println("Updated.");
                        break;
                    }
                    case 3: {
                        String id = ConsoleIO.line("Asset ID  : ");
                        String dept = ConsoleIO.line("Department: ");
                        assetService.allocate(id, dept);
                        System.out.println("Allocated.");
                        break;
                    }
                    case 4:
                        assetService.release(ConsoleIO.line("Asset ID: "));
                        System.out.println("Released.");
                        break;
                    case 5: {
                        String id = ConsoleIO.line("Asset ID: ");
                        if (ConsoleIO.confirm("Delete " + id + "?")) {
                            assetService.delete(id);
                            System.out.println("Deleted.");
                        }
                        break;
                    }
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

    // ---------- 4. billing config ----------
    private void billingMenu() {
        int c;
        do {
            System.out.println("\n--- Billing Configuration ---");
            System.out.println("  Rates: " + billingConfigService.getConfig());
            System.out.println("  Accepted insurance networks:");
            for (InsuranceNetwork n : billingConfigService.networks()) System.out.println("    " + n);
            System.out.println("1. Set base consultation rate   2. Set follow-up rate   3. Set currency");
            System.out.println("4. Add insurance network   5. Update network   6. Remove network   0. Back");
            c = ConsoleIO.readInt("Choice: ");
            try {
                switch (c) {
                    case 1:
                        billingConfigService.setBaseConsultationRate(ConsoleIO.readDouble("Base rate: "));
                        System.out.println("Saved.");
                        break;
                    case 2:
                        billingConfigService.setFollowUpRate(ConsoleIO.readDouble("Follow-up rate: "));
                        System.out.println("Saved.");
                        break;
                    case 3:
                        billingConfigService.setCurrency(ConsoleIO.line("Currency code: "));
                        System.out.println("Saved.");
                        break;
                    case 4: {
                        String code = ConsoleIO.required("Network code: ");
                        String name = ConsoleIO.line("Network name: ");
                        double cov = ConsoleIO.readDouble("Coverage % (0-100): ");
                        billingConfigService.addNetwork(code, name, cov);
                        System.out.println("Added.");
                        break;
                    }
                    case 5: {
                        String code = ConsoleIO.line("Network code: ");
                        String name = ConsoleIO.line("New name (blank=keep): ");
                        Double cov = ConsoleIO.readDoubleOrNull("New coverage % (blank=keep): ");
                        String act = ConsoleIO.line("Active? (y/n, blank=keep): ");
                        Boolean active = act.isBlank() ? null : act.equalsIgnoreCase("y");
                        billingConfigService.updateNetwork(code, name.isBlank() ? null : name, cov, active);
                        System.out.println("Updated.");
                        break;
                    }
                    case 6:
                        billingConfigService.removeNetwork(ConsoleIO.line("Network code: "));
                        System.out.println("Removed.");
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

    // ---------- 5. lab / imaging requests ----------
    private void labMenu() {
        int c;
        do {
            System.out.println("\n--- Lab / Imaging Requests ---");
            for (LabRequest r : Database.labRequests) System.out.println("  " + r);
            System.out.println("1. Schedule a request onto an asset   2. Record result / complete");
            System.out.println("3. Cancel a request   0. Back");
            c = ConsoleIO.readInt("Choice: ");
            try {
                switch (c) {
                    case 1: {
                        String rid = ConsoleIO.line("Request ID: ");
                        String ridKey = rid.trim();
                        LabRequest r = null;
                        for (LabRequest x : Database.labRequests) {
                            if (x.getRequestId().equalsIgnoreCase(ridKey)) {
                                r = x;
                                break;
                            }
                        }
                        if (r == null) throw new IllegalArgumentException("No such request: " + rid);
                        System.out.println("Available " + r.getType().getRequiredAsset() + " assets:");
                        for (HospitalAsset a : assetService.availableByType(r.getType().getRequiredAsset())) {
                            System.out.println("  " + a);
                        }
                        String aid = ConsoleIO.line("Asset ID: ");
                        labRequestService.schedule(rid, aid);
                        System.out.println("Scheduled.");
                        break;
                    }
                    case 2: {
                        String rid = ConsoleIO.line("Request ID: ");
                        String notes = ConsoleIO.line("Result notes: ");
                        labRequestService.complete(rid, notes);
                        System.out.println("Completed.");
                        break;
                    }
                    case 3:
                        labRequestService.cancel(ConsoleIO.line("Request ID: "));
                        System.out.println("Cancelled.");
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

    // ---------- 6. profile ----------
    private void profileMenu() {
        System.out.println("\nName: " + current.getName() + " | Phone: " + current.getPhone()
                + " | Email: " + current.getEmail());
        String name = ConsoleIO.line("New name (blank=keep)   : ");
        String phone = ConsoleIO.line("New phone (blank=keep)  : ");
        String email = ConsoleIO.line("New email (blank=keep)  : ");
        String address = ConsoleIO.line("New address (blank=keep): ");
        accountService.editProfile(current, name, phone, email, address, null);
        System.out.println("Profile saved.");
        if (ConsoleIO.confirm("Change password?")) {
            String cur = ConsoleIO.line("Current password: ");
            String next = ConsoleIO.line("New password (min 6): ");
            System.out.println(accountService.changePassword(current, cur, next)
                    ? "Password changed." : "Rejected.");
        }
    }
}
