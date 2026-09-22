package assignment.service;

import assignment.model.Appointment;
import assignment.model.Feedback;
import assignment.model.Patient;
import assignment.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Use-case: a patient submits ratings and comments for a doctor and a clinic visit. */
public class FeedbackService {

    public Feedback submit(String patientId, String doctorId, String appointmentId,
                           int doctorRating, int visitRating, String comment) {
        String key = (patientId == null) ? "" : patientId.trim();
        boolean patientExists = false;
        for (Patient p : Database.patients) {
            if (p.getId().equalsIgnoreCase(key)) {
                patientExists = true;
                break;
            }
        }
        if (!patientExists) throw new IllegalArgumentException("No such patient: " + patientId);
        checkRating(doctorRating, "Doctor");
        checkRating(visitRating, "Visit");

        String resolvedDoctor = doctorId;
        String resolvedAppt = null;
        if (appointmentId != null && !appointmentId.isBlank()) {
            Appointment appt = null;
            for (Appointment a : Database.appointments) {
                if (a.getAppointmentId().equalsIgnoreCase(appointmentId.trim())) {
                    appt = a;
                    break;
                }
            }
            if (appt == null) throw new IllegalArgumentException("No such appointment: " + appointmentId);
            if (!appt.getPatientId().equalsIgnoreCase(key))
                throw new IllegalArgumentException("That appointment is not yours");
            resolvedDoctor = appt.getDoctorId();
            resolvedAppt = appt.getAppointmentId();
        }
        if (resolvedDoctor == null || resolvedDoctor.isBlank())
            throw new IllegalArgumentException("A doctor or an appointment id is required");

        List<String> existingIds = new ArrayList<>();
        for (Feedback f : Database.feedback) existingIds.add(f.getFeedbackId());
        String id = IdGenerator.next("FB", existingIds);

        Feedback fb = new Feedback(id, key, resolvedDoctor, resolvedAppt, LocalDateTime.now(),
                doctorRating, visitRating, comment == null ? "" : comment.trim());
        Database.feedback.add(fb);
        Database.saveFeedback();
        return fb;
    }

    public double averageDoctorRating(String doctorId) {
        int total = 0;
        int count = 0;
        for (Feedback f : Database.feedback) {
            if (f.getDoctorId() != null && f.getDoctorId().equalsIgnoreCase(doctorId)) {
                total += f.getDoctorRating();
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) total / count;
    }

    public List<Feedback> forDoctor(String doctorId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : Database.feedback) {
            if (f.getDoctorId() != null && f.getDoctorId().equalsIgnoreCase(doctorId)) result.add(f);
        }
        sortBySubmittedAtDescending(result);
        return result;
    }

    public List<Feedback> forPatient(String patientId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : Database.feedback) {
            if (f.getPatientId().equalsIgnoreCase(patientId)) result.add(f);
        }
        sortBySubmittedAtDescending(result);
        return result;
    }

    private void sortBySubmittedAtDescending(List<Feedback> list) {
        for (int i = 1; i < list.size(); i++) {
            Feedback current = list.get(i);
            int j = i - 1;
            while (j >= 0 && list.get(j).getSubmittedAt().isBefore(current.getSubmittedAt())) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, current);
        }
    }

    private void checkRating(int r, String what) {
        if (r < 1 || r > 5)
            throw new IllegalArgumentException(what + " rating must be between 1 and 5");
    }
}
