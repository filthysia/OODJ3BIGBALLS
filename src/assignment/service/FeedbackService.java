package assignment.service;

import assignment.model.Appointment;
import assignment.model.Feedback;
import assignment.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Use-case: a patient submits ratings and comments for a doctor and a clinic visit. */
public class FeedbackService {

    public Feedback submit(String patientId, String doctorId, String appointmentId,
                           int doctorRating, int visitRating, String comment) {
        String key = (patientId == null) ? "" : patientId.trim();
        if (Database.patients.stream().noneMatch(p -> p.getId().equalsIgnoreCase(key)))
            throw new IllegalArgumentException("No such patient: " + patientId);
        checkRating(doctorRating, "Doctor");
        checkRating(visitRating, "Visit");

        String resolvedDoctor = doctorId;
        String resolvedAppt = null;
        if (appointmentId != null && !appointmentId.isBlank()) {
            Appointment appt = Database.appointments.stream()
                    .filter(a -> a.getAppointmentId().equalsIgnoreCase(appointmentId.trim()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No such appointment: " + appointmentId));
            if (!appt.getPatientId().equalsIgnoreCase(key))
                throw new IllegalArgumentException("That appointment is not yours");
            resolvedDoctor = appt.getDoctorId();
            resolvedAppt = appt.getAppointmentId();
        }
        if (resolvedDoctor == null || resolvedDoctor.isBlank())
            throw new IllegalArgumentException("A doctor or an appointment id is required");

        String id = IdGenerator.next("FB", Database.feedback.stream()
                .map(Feedback::getFeedbackId).collect(Collectors.toList()));
        Feedback fb = new Feedback(id, key, resolvedDoctor, resolvedAppt, LocalDateTime.now(),
                doctorRating, visitRating, comment == null ? "" : comment.trim());
        Database.feedback.add(fb);
        Database.saveFeedback();
        return fb;
    }

    public double averageDoctorRating(String doctorId) {
        return Database.feedback.stream()
                .filter(f -> f.getDoctorId() != null && f.getDoctorId().equalsIgnoreCase(doctorId))
                .mapToInt(Feedback::getDoctorRating)
                .average()
                .orElse(0.0);
    }

    public List<Feedback> forDoctor(String doctorId) {
        return Database.feedback.stream()
                .filter(f -> f.getDoctorId() != null && f.getDoctorId().equalsIgnoreCase(doctorId))
                .sorted(Comparator.comparing(Feedback::getSubmittedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Feedback> forPatient(String patientId) {
        return Database.feedback.stream()
                .filter(f -> f.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(Feedback::getSubmittedAt).reversed())
                .collect(Collectors.toList());
    }

    private void checkRating(int r, String what) {
        if (r < 1 || r > 5)
            throw new IllegalArgumentException(what + " rating must be between 1 and 5");
    }
}
