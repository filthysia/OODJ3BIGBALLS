package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDateTime;

/** A patient's rating and comment for a doctor and a clinic visit. */
public class Feedback {

    private String feedbackId;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private LocalDateTime submittedAt;
    private int doctorRating;   // 1..5
    private int visitRating;    // 1..5
    private String comment;

    public Feedback() { }

    public Feedback(String feedbackId, String patientId, String doctorId, String appointmentId,
                    LocalDateTime submittedAt, int doctorRating, int visitRating, String comment) {
        this.feedbackId = feedbackId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.submittedAt = submittedAt;
        this.doctorRating = doctorRating;
        this.visitRating = visitRating;
        this.comment = comment;
    }

    public String getFeedbackId() { return feedbackId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public int getDoctorRating() { return doctorRating; }
    public int getVisitRating() { return visitRating; }
    public String getComment() { return comment; }

    public String toCsv() {
        return CsvUtil.join(feedbackId, patientId, doctorId, appointmentId, submittedAt,
                doctorRating, visitRating, comment);
    }

    public static Feedback fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Feedback(f[0], f[1], f[2],
                f[3].isBlank() ? null : f[3],
                LocalDateTime.parse(f[4]),
                f[5].isBlank() ? 0 : Integer.parseInt(f[5]),
                f[6].isBlank() ? 0 : Integer.parseInt(f[6]),
                f.length > 7 ? f[7] : "");
    }

    @Override
    public String toString() {
        return String.format("%s  doctor=%d/5 visit=%d/5  \"%s\"",
                submittedAt.toLocalDate(), doctorRating, visitRating, comment);
    }
}
