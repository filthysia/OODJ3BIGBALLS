package assignment.service;

import assignment.model.ConsultationNote;
import assignment.model.VitalSigns;
import assignment.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Use-case: a doctor logs patient vital signs and writes consultation notes. */
public class ConsultationService {

    public VitalSigns logVitals(String patientId, String doctorId, String appointmentId,
                                double temperatureC, int systolic, int diastolic, int heartRate,
                                int respiratoryRate, int spo2, double weightKg, double heightCm) {
        requirePatient(patientId);
        String id = IdGenerator.next("VIT", Database.vitals.stream()
                .map(VitalSigns::getRecordId).collect(Collectors.toList()));
        VitalSigns v = new VitalSigns(id, patientId.trim(), doctorId, blank(appointmentId),
                LocalDateTime.now(), temperatureC, systolic, diastolic, heartRate,
                respiratoryRate, spo2, weightKg, heightCm);
        Database.vitals.add(v);
        Database.saveVitals();
        return v;
    }

    public ConsultationNote writeNote(String appointmentId, String patientId, String doctorId,
                                      String symptoms, String diagnosis, String notes) {
        requirePatient(patientId);
        String id = IdGenerator.next("CN", Database.consultationNotes.stream()
                .map(ConsultationNote::getNoteId).collect(Collectors.toList()));
        ConsultationNote n = new ConsultationNote(id, blank(appointmentId), patientId.trim(), doctorId,
                LocalDateTime.now(), nz(symptoms), nz(diagnosis), nz(notes));
        Database.consultationNotes.add(n);
        Database.saveConsultationNotes();
        return n;
    }

    public List<VitalSigns> vitalsForPatient(String patientId) {
        return Database.vitals.stream()
                .filter(v -> v.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(VitalSigns::getRecordedAt))
                .collect(Collectors.toList());
    }

    public List<ConsultationNote> notesForPatient(String patientId) {
        return Database.consultationNotes.stream()
                .filter(n -> n.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(ConsultationNote::getWrittenAt))
                .collect(Collectors.toList());
    }

    private void requirePatient(String id) {
        String key = (id == null) ? "" : id.trim();
        if (Database.patients.stream().noneMatch(p -> p.getId().equalsIgnoreCase(key)))
            throw new IllegalArgumentException("No such patient: " + id);
    }

    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
