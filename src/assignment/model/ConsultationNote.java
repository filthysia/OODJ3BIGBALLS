package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDateTime;

/** A doctor's written record of one consultation. */
public class ConsultationNote {

    private String noteId;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime writtenAt;
    private String symptoms;
    private String diagnosis;
    private String notes;

    public ConsultationNote() { }

    public ConsultationNote(String noteId, String appointmentId, String patientId, String doctorId,
                            LocalDateTime writtenAt, String symptoms, String diagnosis, String notes) {
        this.noteId = noteId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.writtenAt = writtenAt;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.notes = notes;
    }

    public String getNoteId() { return noteId; }
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getWrittenAt() { return writtenAt; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String toCsv() {
        return CsvUtil.join(noteId, appointmentId, patientId, doctorId, writtenAt,
                symptoms, diagnosis, notes);
    }

    public static ConsultationNote fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new ConsultationNote(f[0], f[1], f[2], f[3], LocalDateTime.parse(f[4]),
                f.length > 5 ? f[5] : "", f.length > 6 ? f[6] : "", f.length > 7 ? f[7] : "");
    }

    @Override
    public String toString() {
        return String.format("%s  Dx: %s | %s", writtenAt, diagnosis, notes);
    }
}
