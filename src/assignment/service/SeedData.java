package assignment.service;

import assignment.model.AdminStaff;
import assignment.model.AssetStatus;
import assignment.model.AssetType;
import assignment.model.ClinicConfig;
import assignment.model.Department;
import assignment.model.DepartmentStatus;
import assignment.model.Doctor;
import assignment.model.Gender;
import assignment.model.HospitalAsset;
import assignment.model.InsuranceNetwork;
import assignment.model.MedicalManager;
import assignment.model.Patient;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.ShiftStatus;
import assignment.model.ShiftType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/** Writes a small starter dataset the first time any module runs. */
public final class SeedData {

    private SeedData() { }

    public static void ensure() {
        boolean anyUsers = !Database.managers.isEmpty() || !Database.adminStaff.isEmpty()
                || !Database.doctors.isEmpty() || !Database.patients.isEmpty();
        if (anyUsers) return;

        Database.managers.add(new MedicalManager("MM001", "Lim Wei Sheng", "900101-14-5566",
                Gender.MALE, "012-3456789", "manager@apumc.edu.my", "Bukit Jalil, KL",
                "admin123", "ADMIN", LocalDate.of(2020, 1, 6), 12000, "Level 3, Admin Wing"));
        Database.saveManagers();

        Database.adminStaff.add(new AdminStaff("AS001", "Nadia Ismail", "930303-10-2244",
                Gender.FEMALE, "012-7778888", "frontdesk@apumc.edu.my", "Seri Kembangan, Selangor",
                "admin123", "ADMIN", LocalDate.of(2022, 2, 1), 4500, "Front Desk, Level 1"));
        Database.saveAdminStaff();

        Database.doctors.add(new Doctor("DOC001", "Aisha Rahman", "850505-10-1122", Gender.FEMALE,
                "013-1112222", "aisha@apumc.edu.my", "Cheras, KL", "doc123", "CARD",
                LocalDate.of(2021, 3, 1), 9000, "Interventional Cardiology", true, "MM001"));
        Database.doctors.add(new Doctor("DOC002", "John Tan", "820202-08-3344", Gender.MALE,
                "014-3334444", "john@apumc.edu.my", "Petaling Jaya, Selangor", "doc123", "CARD",
                LocalDate.of(2019, 7, 15), 9500, "Electrophysiology", true, "MM001"));
        Database.saveDoctors();

        Database.patients.add(new Patient("PT001", "Meera Suppiah", "960606-14-5566", Gender.FEMALE,
                "016-2223333", "meera@example.com", "Bukit Jalil, KL", "pass123",
                "O+", "Penicillin", LocalDate.of(2024, 5, 20)));
        Database.savePatients();

        Department card = new Department("CARD", "Cardiology", "Heart and vascular care",
                "DOC001", 150.0, DepartmentStatus.ACTIVE);
        card.addDoctor("DOC001");
        card.addDoctor("DOC002");
        Database.departments.add(card);
        Database.saveDepartments();

        Database.assets.add(new HospitalAsset("AST0001", "Consultation Room 1", AssetType.CONSULTATION_ROOM,
                "Level 2", 1, AssetStatus.AVAILABLE, null));
        Database.assets.add(new HospitalAsset("AST0002", "Ward A", AssetType.INPATIENT_WARD,
                "Level 4", 8, AssetStatus.AVAILABLE, null));
        Database.assets.add(new HospitalAsset("AST0003", "Pathology Lab", AssetType.LAB,
                "Level 1", 4, AssetStatus.AVAILABLE, null));
        Database.assets.add(new HospitalAsset("AST0004", "X-Ray Room 1", AssetType.IMAGING_ROOM,
                "Level 1", 1, AssetStatus.AVAILABLE, null));
        Database.saveAssets();

        Database.insuranceNetworks.add(new InsuranceNetwork("AIA", "AIA Malaysia", 80.0, true));
        Database.insuranceNetworks.add(new InsuranceNetwork("GE", "Great Eastern", 70.0, true));
        Database.saveInsuranceNetworks();

        Database.clinicConfig = new ClinicConfig(80.0, 50.0, "RM");
        Database.saveClinicConfig();

        // a roster for next week so patients always have open (future) slots to book
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        ShiftRoster roster = new ShiftRoster("RST0001", "CARD", monday, "MM001");
        Database.rosters.add(roster);
        Shift s1 = new Shift("SH0001", "RST0001", "DOC001", monday, ShiftType.MORNING,
                ShiftType.MORNING.getDefaultStart(), ShiftType.MORNING.getDefaultEnd(), ShiftStatus.SCHEDULED);
        Shift s2 = new Shift("SH0002", "RST0001", "DOC002", monday.plusDays(1), ShiftType.AFTERNOON,
                ShiftType.AFTERNOON.getDefaultStart(), ShiftType.AFTERNOON.getDefaultEnd(), ShiftStatus.SCHEDULED);
        Database.shifts.add(s1);
        Database.shifts.add(s2);
        roster.getShifts().add(s1);
        roster.getShifts().add(s2);
        Database.saveRostersAndShifts();

        System.out.println("(First run: seeded starter data)");
        System.out.println("  Medical Manager : MM001 / admin123");
        System.out.println("  Admin Staff     : AS001 / admin123");
        System.out.println("  Doctor          : DOC001 / doc123   (also DOC002)");
        System.out.println("  Patient         : PT001 / pass123");
    }
}
