package assignment.service;

import assignment.model.Appointment;
import assignment.model.AppointmentStatus;
import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Patient;
import assignment.model.Shift;
import assignment.model.ShiftStatus;
import assignment.model.Slot;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Use-case: a patient browses a doctor's open slots and books / reschedules / cancels. */
public class BookingService {

    /** Length of one bookable consultation slot, in minutes. */
    public static final int SLOT_MINUTES = 30;

    private final DepartmentService departmentService = new DepartmentService();

    /** Free consultation slots for a doctor on a date, derived from the published roster. */
    public List<Slot> availableSlots(String doctorId, LocalDate date) {
        List<Slot> slots = new ArrayList<>();
        for (Shift shift : Database.shifts) {
            boolean matches = shift.getDoctorId().equalsIgnoreCase(doctorId)
                    && shift.getDate().equals(date)
                    && shift.getStatus() == ShiftStatus.SCHEDULED;
            if (!matches) continue;

            LocalTime t = shift.getStart();
            while (!t.plusMinutes(SLOT_MINUTES).isAfter(shift.getEnd())) {
                LocalTime end = t.plusMinutes(SLOT_MINUTES);
                if (isFree(doctorId, date, t)) slots.add(new Slot(doctorId, date, t, end));
                t = end;
            }
        }
        sortByStart(slots);
        return slots;
    }

    public Appointment book(String patientId, String doctorId, LocalDate date, LocalTime start) {
        requirePatient(patientId);
        Doctor doctor = requireDoctor(doctorId);
        if (!hasSlotAt(availableSlots(doctorId, date), start))
            throw new IllegalArgumentException("That slot is not available - check the doctor's open slots");

        List<String> existingIds = new ArrayList<>();
        for (Appointment a : Database.appointments) existingIds.add(a.getAppointmentId());
        String id = IdGenerator.next("APT", existingIds);

        Appointment appt = new Appointment(id, patientId.trim(), doctor.getId(), doctor.getDepartmentCode(),
                LocalDateTime.of(date, start), AppointmentStatus.BOOKED, feeFor(doctor));
        Database.appointments.add(appt);
        Database.saveAppointments();
        return appt;
    }

    public Appointment reschedule(String appointmentId, LocalDate date, LocalTime start) {
        Appointment appt = require(appointmentId);
        if (appt.getStatus() != AppointmentStatus.BOOKED)
            throw new IllegalStateException("Only booked appointments can be rescheduled");
        boolean sameSlot = appt.getDateTime().toLocalDate().equals(date)
                && appt.getDateTime().toLocalTime().equals(start);
        boolean free = sameSlot || hasSlotAt(availableSlots(appt.getDoctorId(), date), start);
        if (!free) throw new IllegalArgumentException("That slot is not available");
        appt.setDateTime(LocalDateTime.of(date, start));
        Database.saveAppointments();
        return appt;
    }

    public void cancel(String appointmentId) {
        Appointment appt = require(appointmentId);
        if (appt.getStatus() == AppointmentStatus.COMPLETED)
            throw new IllegalStateException("Completed appointments cannot be cancelled");
        appt.setStatus(AppointmentStatus.CANCELLED);
        Database.saveAppointments();
    }

    public void markCompleted(String appointmentId) {
        Appointment appt = require(appointmentId);
        appt.setStatus(AppointmentStatus.COMPLETED);
        Database.saveAppointments();
    }

    public void markNoShow(String appointmentId) {
        Appointment appt = require(appointmentId);
        appt.setStatus(AppointmentStatus.NO_SHOW);
        Database.saveAppointments();
    }

    public List<Appointment> forPatient(String patientId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : Database.appointments) {
            if (a.getPatientId().equalsIgnoreCase(patientId)) result.add(a);
        }
        sortByDateTime(result);
        return result;
    }

    public List<Appointment> forDoctor(String doctorId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : Database.appointments) {
            if (a.getDoctorId().equalsIgnoreCase(doctorId)) result.add(a);
        }
        sortByDateTime(result);
        return result;
    }

    // ----- helpers -----

    private boolean hasSlotAt(List<Slot> slots, LocalTime start) {
        for (Slot s : slots) {
            if (s.getStart().equals(start)) return true;
        }
        return false;
    }

    private void sortByStart(List<Slot> slots) {
        // simple insertion sort - the list of open slots for one day is short
        for (int i = 1; i < slots.size(); i++) {
            Slot current = slots.get(i);
            int j = i - 1;
            while (j >= 0 && slots.get(j).getStart().isAfter(current.getStart())) {
                slots.set(j + 1, slots.get(j));
                j--;
            }
            slots.set(j + 1, current);
        }
    }

    private void sortByDateTime(List<Appointment> appointments) {
        for (int i = 1; i < appointments.size(); i++) {
            Appointment current = appointments.get(i);
            int j = i - 1;
            while (j >= 0 && appointments.get(j).getDateTime().isAfter(current.getDateTime())) {
                appointments.set(j + 1, appointments.get(j));
                j--;
            }
            appointments.set(j + 1, current);
        }
    }

    private boolean isFree(String doctorId, LocalDate date, LocalTime start) {
        LocalDateTime at = LocalDateTime.of(date, start);
        for (Appointment a : Database.appointments) {
            boolean clashes = a.getDoctorId().equalsIgnoreCase(doctorId)
                    && a.getStatus() != AppointmentStatus.CANCELLED
                    && a.getDateTime().equals(at);
            if (clashes) return false;
        }
        return true;
    }

    private double feeFor(Doctor doctor) {
        if (doctor.getDepartmentCode() != null) {
            Department dept = departmentService.findByCode(doctor.getDepartmentCode());
            if (dept != null && dept.getConsultationFee() > 0) return dept.getConsultationFee();
        }
        return Database.clinicConfig.getBaseConsultationRate();
    }

    private void requirePatient(String id) {
        String key = (id == null) ? "" : id.trim();
        for (Patient p : Database.patients) {
            if (p.getId().equalsIgnoreCase(key)) return;
        }
        throw new IllegalArgumentException("No such patient: " + id);
    }

    private Doctor requireDoctor(String id) {
        String key = (id == null) ? "" : id.trim();
        for (Doctor d : Database.doctors) {
            if (d.getId().equalsIgnoreCase(key)) return d;
        }
        throw new IllegalArgumentException("No such doctor: " + id);
    }

    private Appointment require(String id) {
        String key = (id == null) ? "" : id.trim();
        for (Appointment a : Database.appointments) {
            if (a.getAppointmentId().equalsIgnoreCase(key)) return a;
        }
        throw new IllegalArgumentException("No such appointment: " + id);
    }
}
