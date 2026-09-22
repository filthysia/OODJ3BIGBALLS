package assignment.service;

import assignment.model.ConsultationNote;
import assignment.model.Patient;
import assignment.model.VitalSigns;
import assignment.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Use-case: a doctor logs patient vital signs and writes consultation notes. */
public class ConsultationService {

    public VitalSigns logVitals(String patientId, String doctorId, String appointmentId,
                                double temperatureC, int systolic, int diastolic, int heartRate,
                                int respiratoryRate, int spo2, double weightKg, double heightCm) {
        requirePatient(patientId);
        List<String> existingIds = new ArrayList<>();
        for (VitalSigns v : Database.vitals) existingIds.add(v.getRecordId());
        String id = IdGenerator.next("VIT", existingIds);

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
        List<String> existingIds = new ArrayList<>();
        for (ConsultationNote n : Database.consultationNotes) existingIds.add(n.getNoteId());
        String id = IdGenerator.next("CN", existingIds);

        ConsultationNote n = new ConsultationNote(id, blank(appointmentId), patientId.trim(), doctorId,
                LocalDateTime.now(), nz(symptoms), nz(diagnosis), nz(notes));
        Database.consultationNotes.add(n);
        Database.saveConsultationNotes();
        return n;
    }

    public List<VitalSigns> vitalsForPatient(String patientId) {
        List<VitalSigns> result = new ArrayList<>();
        for (VitalSigns v : Database.vitals) {
            if (v.getPatientId().equalsIgnoreCase(patientId)) result.add(v);
        }
        // insertion sort by recorded time - the list per patient is short
        for (int i = 1; i < result.size(); i++) {
            VitalSigns current = result.get(i);
            int j = i - 1;
            while (j >= 0 && result.get(j).getRecordedAt().isAfter(current.getRecordedAt())) {
                result.set(j + 1, result.get(j));
                j--;
            }
            result.set(j + 1, current);
        }
        return result;
    }

    public List<ConsultationNote> notesForPatient(String patientId) {
        List<ConsultationNote> result = new ArrayList<>();
        for (ConsultationNote n : Database.consultationNotes) {
            if (n.getPatientId().equalsIgnoreCase(patientId)) result.add(n);
        }
        for (int i = 1; i < result.size(); i++) {
            ConsultationNote current = result.get(i);
            int j = i - 1;
            while (j >= 0 && result.get(j).getWrittenAt().isAfter(current.getWrittenAt())) {
                result.set(j + 1, result.get(j));
                j--;
            }
            result.set(j + 1, current);
        }
        return result;
    }

    private void requirePatient(String id) {
        String key = (id == null) ? "" : id.trim();
        for (Patient p : Database.patients) {
            if (p.getId().equalsIgnoreCase(key)) return;
        }
        throw new IllegalArgumentException("No such patient: " + id);
    }

    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
