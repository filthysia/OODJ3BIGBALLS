package assignment.app;

import assignment.service.Database;
import assignment.service.SeedData;

/**
 * Umbrella launcher for the Hospital Management System. Loads the data files once,
 * then routes to the chosen role module. Each module can also be run on its own
 * via its own {@code main} method.
 */
public class HospitalApp {

    public static void main(String[] args) {
        Database.loadAll();
        SeedData.ensure();

        int choice;
        do {
            System.out.println("\n============================================");
            System.out.println("     APU MEDICAL CENTRE - Management System   ");
            System.out.println("============================================");
            System.out.println("1. Medical Manager");
            System.out.println("2. Admin Staff");
            System.out.println("3. Doctor");
            System.out.println("4. Patient");
            System.out.println("0. Exit");
            choice = ConsoleIO.readInt("Select role: ");
            switch (choice) {
                case 1 -> new MedicalManagerApp().start();
                case 2 -> new AdminStaffApp().start();
                case 3 -> new DoctorApp().start();
                case 4 -> new PatientApp().start();
                case 0 -> System.out.println("Goodbye.");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }
}
