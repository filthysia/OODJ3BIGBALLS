package assignment.service;

import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.model.PrescriptionStatus;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Use-case: a doctor issues digital medication prescriptions to a patient's record. */
public class PrescriptionService {

    public Prescription issue(String patientId, String doctorId, String appointmentId) {
        String key = (patientId == null) ? "" : patientId.trim();
        if (Database.patients.stream().noneMatch(p -> p.getId().equalsIgnoreCase(key)))
            throw new IllegalArgumentException("No such patient: " + patientId);

        String id = IdGenerator.next("RX", Database.prescriptions.stream()
                .map(Prescription::getPrescriptionId).collect(Collectors.toList()));
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

        String id = IdGenerator.next("PI", Database.prescriptionItems.stream()
                .map(PrescriptionItem::getItemId).collect(Collectors.toList()));
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
        return Database.prescriptions.stream()
                .filter(p -> p.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(Prescription::getIssuedDate).reversed())
                .collect(Collectors.toList());
    }

    private Prescription require(String id) {
        String key = (id == null) ? "" : id.trim();
        return Database.prescriptions.stream()
                .filter(p -> p.getPrescriptionId().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such prescription: " + id));
    }

    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
