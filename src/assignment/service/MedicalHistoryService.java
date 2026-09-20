package assignment.service;

import assignment.model.Appointment;
import assignment.model.ConsultationNote;
import assignment.model.LabRequest;
import assignment.model.Patient;
import assignment.model.Prescription;
import assignment.model.PrescriptionItem;
import assignment.model.VitalSigns;

import java.util.List;

/** Use-case: a patient views their personal medical history and prescriptions. */
public class MedicalHistoryService {

    private final BookingService bookingService = new BookingService();
    private final ConsultationService consultationService = new ConsultationService();
    private final PrescriptionService prescriptionService = new PrescriptionService();
    private final LabRequestService labRequestService = new LabRequestService();

    public String historyReport(String patientId) {
        Patient patient = Database.patients.stream()
                .filter(p -> p.getId().equalsIgnoreCase(patientId == null ? "" : patientId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such patient: " + patientId));

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("  MEDICAL HISTORY - ").append(patient.getName())
          .append(" (").append(patient.getId()).append(")\n");
        sb.append("=====================================================\n");
        sb.append("Blood type : ").append(nz(patient.getBloodType())).append('\n');
        sb.append("Allergies  : ").append(nz(patient.getAllergies())).append('\n');

        sb.append("\n-- Appointments --\n");
        List<Appointment> appts = bookingService.forPatient(patient.getId());
        if (appts.isEmpty()) sb.append("  (none)\n");
        for (Appointment a : appts) {
            sb.append(String.format("  %-9s %s  Dr %-8s %-6s %-10s RM%.2f%n",
                    a.getAppointmentId(), a.getDateTime(), a.getDoctorId(),
                    a.getDepartmentCode(), a.getStatus(), a.getConsultationFee()));
        }

        sb.append("\n-- Vital signs --\n");
        List<VitalSigns> vitals = consultationService.vitalsForPatient(patient.getId());
        if (vitals.isEmpty()) sb.append("  (none)\n");
        for (VitalSigns v : vitals) sb.append("  ").append(v).append('\n');

        sb.append("\n-- Consultation notes --\n");
        List<ConsultationNote> notes = consultationService.notesForPatient(patient.getId());
        if (notes.isEmpty()) sb.append("  (none)\n");
        for (ConsultationNote n : notes) {
            sb.append("  ").append(n.getWrittenAt()).append("  Dr ").append(n.getDoctorId()).append('\n');
            sb.append("      Symptoms : ").append(nz(n.getSymptoms())).append('\n');
            sb.append("      Diagnosis: ").append(nz(n.getDiagnosis())).append('\n');
            sb.append("      Notes    : ").append(nz(n.getNotes())).append('\n');
        }

        sb.append("\n-- Prescriptions --\n");
        List<Prescription> rxs = prescriptionService.forPatient(patient.getId());
        if (rxs.isEmpty()) sb.append("  (none)\n");
        for (Prescription rx : rxs) {
            sb.append(String.format("  %-9s %s  %-9s (Dr %s)%n",
                    rx.getPrescriptionId(), rx.getIssuedDate(), rx.getStatus(), rx.getDoctorId()));
            for (PrescriptionItem it : rx.getItems()) sb.append("        ").append(it).append('\n');
        }

        sb.append("\n-- Lab / imaging requests --\n");
        List<LabRequest> labs = labRequestService.forPatient(patient.getId());
        if (labs.isEmpty()) sb.append("  (none)\n");
        for (LabRequest r : labs) sb.append("  ").append(r).append('\n');

        sb.append("=====================================================\n");
        return sb.toString();
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
