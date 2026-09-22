package assignment.service;

import assignment.model.Patient;
import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.model.PrescriptionStatus;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Use-case: a doctor issues digital medication prescriptions to a patient's record. */
public class PrescriptionService {

    public Prescription issue(String patientId, String doctorId, String appointmentId) {
        String key = (patientId == null) ? "" : patientId.trim();
        boolean patientExists = false;
        for (Patient p : Database.patients) {
            if (p.getId().equalsIgnoreCase(key)) {
                patientExists = true;
                break;
            }
        }
        if (!patientExists) throw new IllegalArgumentException("No such patient: " + patientId);

        List<String> existingIds = new ArrayList<>();
        for (Prescription rx : Database.prescriptions) existingIds.add(rx.getPrescriptionId());
        String id = IdGenerator.next("RX", existingIds);

        Prescription rx = new Prescription(id, key, doctorId, blank(appointmentId),
                LocalDate.now(), PrescriptionStatus.ACTIVE);
        Database.prescriptions.add(rx);
        Database.savePrescriptions();
        return rx;
    }

    public PrescriptionItem addItem(String prescriptionId, String drugName, String dosage, String frequency,
                                    int durationDays, int quantity, String instructions) {
        Prescription rx = require(prescriptionId);
        if (rx.getStatus() != PrescriptionStatus.ACTIVE)
            throw new IllegalStateException("Cannot add items to a " + rx.getStatus() + " prescription");
        if (drugName == null || drugName.isBlank())
            throw new IllegalArgumentException("Drug name is required");
        if (durationDays < 0 || quantity < 0)
            throw new IllegalArgumentException("Duration and quantity cannot be negative");

        List<String> existingIds = new ArrayList<>();
        for (PrescriptionItem it : Database.prescriptionItems) existingIds.add(it.getItemId());
        String id = IdGenerator.next("PI", existingIds);

        PrescriptionItem item = new PrescriptionItem(id, rx.getPrescriptionId(), drugName.trim(),
                nz(dosage), nz(frequency), durationDays, quantity, nz(instructions));
        Database.prescriptionItems.add(item);
        rx.getItems().add(item);
        Database.savePrescriptions();
        return item;
    }

    public void cancel(String prescriptionId) {
        Prescription rx = require(prescriptionId);
        rx.setStatus(PrescriptionStatus.CANCELLED);
        Database.savePrescriptions();
    }

    public void markDispensed(String prescriptionId) {
        Prescription rx = require(prescriptionId);
        if (rx.getItems().isEmpty())
            throw new IllegalStateException("Prescription has no items");
        rx.setStatus(PrescriptionStatus.DISPENSED);
        Database.savePrescriptions();
    }

    public List<Prescription> forPatient(String patientId) {
        List<Prescription> result = new ArrayList<>();
        for (Prescription p : Database.prescriptions) {
            if (p.getPatientId().equalsIgnoreCase(patientId)) result.add(p);
        }
        // insertion sort by issued date, newest first
        for (int i = 1; i < result.size(); i++) {
            Prescription current = result.get(i);
            int j = i - 1;
            while (j >= 0 && result.get(j).getIssuedDate().isBefore(current.getIssuedDate())) {
                result.set(j + 1, result.get(j));
                j--;
            }
            result.set(j + 1, current);
        }
        return result;
    }

    private Prescription require(String id) {
        String key = (id == null) ? "" : id.trim();
        for (Prescription p : Database.prescriptions) {
            if (p.getPrescriptionId().equalsIgnoreCase(key)) return p;
        }
        throw new IllegalArgumentException("No such prescription: " + id);
    }

    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
