package assignment.tests;

import assignment.model.AssetStatus;
import assignment.model.AssetType;
import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.HospitalAsset;
import assignment.model.LabRequest;
import assignment.model.LabTestType;
import assignment.model.Patient;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.ShiftType;
import assignment.service.AccountService;
import assignment.service.AssetService;
import assignment.service.BillingConfigService;
import assignment.service.BookingService;
import assignment.service.Database;
import assignment.service.DepartmentService;
import assignment.service.LabRequestService;
import assignment.service.RosterService;
import assignment.service.UserService;
import assignment.util.CsvUtil;
import assignment.util.IdGenerator;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written test harness for the service layer - no external testing
 * library, just plain Java and manual checks, matching the classic style
 * used across the rest of the coursework. Run this file directly (it has
 * a main method) the same way any other console app is run.
 *
 * Uses an isolated data folder (see DATA_DIR below) so running the tests
 * never touches the real data/ files the console and GUI apps use.
 */
public class ServiceTests {

    private static final String TEST_DATA_DIR = "test-data-tmp";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        Database.DATA_DIR = TEST_DATA_DIR;
        Database.loadAll();

        try {
            testGenderFromString();
            testIdGeneratorSequencing();
            testCsvUtilRoundTrip();
            testDepartmentCreateAndDuplicateRejected();
            testUserServiceCreateAndDeleteUnknown();
            testAccountServiceChangePassword();
            testBookingRejectsUnknownPatient();
            testBookingAndDoubleBookingRejected();
            testAssetAllocateAndRelease();
            testBillingNetworkDuplicateRejected();
            testLabRequestScheduleTypeMismatchRejected();
        } finally {
            deleteRecursively(new File(TEST_DATA_DIR));
        }

        System.out.println();
        System.out.println("=====================================================");
        System.out.println("  " + passed + " passed, " + failed + " failed");
        System.out.println("=====================================================");
        if (failed > 0) System.exit(1);
    }

    // ----- util / model tests -----

    private static void testGenderFromString() {
        check(Gender.fromString("male") == Gender.MALE, "Gender.fromString is case-insensitive (male)");
        check(Gender.fromString("FEMALE") == Gender.FEMALE, "Gender.fromString is case-insensitive (FEMALE)");
        check(Gender.fromString("nonsense") == Gender.MALE, "Gender.fromString defaults to MALE on bad input");
        check(Gender.fromString(null) == Gender.MALE, "Gender.fromString defaults to MALE on null");
    }

    private static void testIdGeneratorSequencing() {
        List<String> existing = new ArrayList<>();
        existing.add("PT0001");
        existing.add("PT0002");
        existing.add("not-an-id");
        String next = IdGenerator.next("PT", existing);
        check(next.equals("PT0003"), "IdGenerator picks the next free number, ignoring malformed ids (got " + next + ")");

        String first = IdGenerator.next("XY", new ArrayList<>());
        check(first.equals("XY0001"), "IdGenerator starts at 0001 for an empty list (got " + first + ")");
    }

    private static void testCsvUtilRoundTrip() {
        String line = CsvUtil.join("PT0001", "Meera Suppiah", "O+", 42);
        String[] fields = CsvUtil.split(line);
        check(fields.length == 4, "CsvUtil round-trip keeps 4 fields");
        check(fields[0].equals("PT0001") && fields[1].equals("Meera Suppiah"), "CsvUtil round-trip preserves values");

        String sanitized = CsvUtil.join("has,a,comma", "fine");
        check(!sanitized.startsWith("has,a,comma"), "CsvUtil.join strips delimiter characters instead of corrupting the row");
    }

    // ----- service tests -----

    private static void testDepartmentCreateAndDuplicateRejected() {
        DepartmentService departmentService = new DepartmentService();
        Department d = departmentService.create("TST", "Test Ward", "for automated tests", 50.0);
        check(d != null && "TST".equals(d.getCode()), "DepartmentService.create returns the new department");

        boolean rejected = false;
        try {
            departmentService.create("TST", "Duplicate", "", 10.0);
        } catch (IllegalArgumentException ex) {
            rejected = true;
        }
        check(rejected, "DepartmentService.create rejects a duplicate department code");
    }

    private static void testUserServiceCreateAndDeleteUnknown() {
        UserService userService = new UserService();
        Patient p = userService.createPatient("Test Patient", "000000-00-0000", Gender.FEMALE,
                "010-0000000", "test@example.com", "Nowhere", "testpass1", "O+", "None");
        check(p != null && p.getId() != null, "UserService.createPatient returns a saved patient");
        check(userService.find(p.getId()) != null, "The new patient is findable afterwards");

        boolean rejected = false;
        try {
            userService.deleteUser("DOES-NOT-EXIST");
        } catch (IllegalArgumentException ex) {
            rejected = true;
        }
        check(rejected, "UserService.deleteUser rejects an unknown id");
    }

    private static void testAccountServiceChangePassword() {
        UserService userService = new UserService();
        AccountService accountService = new AccountService();
        Patient p = userService.createPatient("Pw Test", "000000-00-0001", Gender.MALE,
                "010-0000001", "pwtest@example.com", "Nowhere", "correctpw", "A+", "None");

        boolean wrongRejected = !accountService.changePassword(p, "totally-wrong", "newpassword1");
        check(wrongRejected, "AccountService.changePassword rejects the wrong current password");

        boolean rightAccepted = accountService.changePassword(p, "correctpw", "newpassword1");
        check(rightAccepted, "AccountService.changePassword accepts the correct current password");
    }

    private static void testBookingRejectsUnknownPatient() {
        UserService userService = new UserService();
        BookingService bookingService = new BookingService();
        Doctor doc = userService.createDoctor("Booking Test Doctor", "000000-00-0002", Gender.MALE,
                "010-0000002", "bookdoc@example.com", "Nowhere", "docpass1", null, "General", null);

        boolean rejected = false;
        try {
            bookingService.book("DOES-NOT-EXIST", doc.getId(), LocalDate.now().plusDays(7), LocalTime.of(9, 0));
        } catch (IllegalArgumentException ex) {
            rejected = true;
        }
        check(rejected, "BookingService.book rejects an unknown patient id");
    }

    private static void testBookingAndDoubleBookingRejected() {
        DepartmentService departmentService = new DepartmentService();
        UserService userService = new UserService();
        RosterService rosterService = new RosterService();
        BookingService bookingService = new BookingService();

        Department dept = departmentService.create("BKT", "Booking Test Dept", "", 80.0);
        Doctor doc = userService.createDoctor("Slot Doctor", "000000-00-0003", Gender.FEMALE,
                "010-0000003", "slotdoc@example.com", "Nowhere", "docpass2", dept.getCode(), "General", null);
        Patient pat = userService.createPatient("Slot Patient", "000000-00-0004", Gender.MALE,
                "010-0000004", "slotpat@example.com", "Nowhere", "patpass2", "B+", "None");

        LocalDate weekDate = LocalDate.now().plusDays(7);
        ShiftRoster roster = rosterService.createRoster(dept.getCode(), weekDate, "MM-TEST");
        Shift shift = rosterService.addShift(roster.getRosterId(), doc.getId(), weekDate, ShiftType.MORNING);
        LocalTime slotStart = shift.getStart();

        bookingService.book(pat.getId(), doc.getId(), weekDate, slotStart);
        check(!bookingService.forPatient(pat.getId()).isEmpty(), "BookingService.book creates a real appointment");

        boolean doubleBookRejected = false;
        try {
            bookingService.book(pat.getId(), doc.getId(), weekDate, slotStart);
        } catch (IllegalArgumentException ex) {
            doubleBookRejected = true;
        }
        check(doubleBookRejected, "BookingService.book rejects booking the same slot twice");
    }

    private static void testAssetAllocateAndRelease() {
        DepartmentService departmentService = new DepartmentService();
        AssetService assetService = new AssetService();

        Department dept = departmentService.create("AST", "Asset Test Dept", "", 60.0);
        HospitalAsset asset = assetService.create("Test Room", AssetType.CONSULTATION_ROOM, "Level 9", 1);
        check(asset.getStatus() == AssetStatus.AVAILABLE, "A newly created asset starts AVAILABLE");

        assetService.allocate(asset.getAssetId(), dept.getCode());
        HospitalAsset reloaded = assetService.findById(asset.getAssetId());
        check(reloaded.getStatus() == AssetStatus.ALLOCATED, "AssetService.allocate marks the asset ALLOCATED");
        check(dept.getCode().equalsIgnoreCase(reloaded.getDepartmentCode()), "AssetService.allocate records the department");

        assetService.release(asset.getAssetId());
        check(assetService.findById(asset.getAssetId()).getStatus() == AssetStatus.AVAILABLE,
                "AssetService.release returns the asset to AVAILABLE");
    }

    private static void testBillingNetworkDuplicateRejected() {
        BillingConfigService billingConfigService = new BillingConfigService();
        billingConfigService.addNetwork("TSTN", "Test Network", 75.0);

        boolean rejected = false;
        try {
            billingConfigService.addNetwork("TSTN", "Duplicate Network", 50.0);
        } catch (IllegalArgumentException ex) {
            rejected = true;
        }
        check(rejected, "BillingConfigService.addNetwork rejects a duplicate network code");
    }

    private static void testLabRequestScheduleTypeMismatchRejected() {
        UserService userService = new UserService();
        AssetService assetService = new AssetService();
        LabRequestService labRequestService = new LabRequestService();

        Patient pat = userService.createPatient("Lab Patient", "000000-00-0005", Gender.FEMALE,
                "010-0000005", "labpat@example.com", "Nowhere", "labpass1", "AB+", "None");
        Doctor doc = userService.createDoctor("Lab Doctor", "000000-00-0006", Gender.MALE,
                "010-0000006", "labdoc@example.com", "Nowhere", "labpass2", null, "Pathology", null);
        HospitalAsset imagingAsset = assetService.create("Imaging Bay", AssetType.IMAGING_ROOM, "Level 1", 1);

        LabRequest req = labRequestService.raise(pat.getId(), doc.getId(), null, LabTestType.BLOOD_TEST, "routine check");
        check(req != null, "LabRequestService.raise creates a request");

        boolean rejected = false;
        try {
            labRequestService.schedule(req.getRequestId(), imagingAsset.getAssetId());
        } catch (IllegalArgumentException ex) {
            rejected = true;
        }
        check(rejected, "LabRequestService.schedule rejects a BLOOD_TEST request onto an IMAGING_ROOM asset");
    }

    // ----- tiny check helper (no external library) -----

    private static void check(boolean condition, String description) {
        if (condition) {
            passed++;
            System.out.println("  PASS - " + description);
        } else {
            failed++;
            System.out.println("  FAIL - " + description);
        }
    }

    private static void deleteRecursively(File file) {
        if (!file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) deleteRecursively(child);
        }
        file.delete();
    }
}
