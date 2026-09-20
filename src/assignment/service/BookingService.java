package assignment.service;

import assignment.model.Appointment;
import assignment.model.AppointmentStatus;
import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Shift;
import assignment.model.ShiftStatus;
import assignment.model.Slot;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Use-case: a patient browses a doctor's open slots and books / reschedules / cancels. */
public class BookingService {

    /** Length of one bookable consultation slot, in minutes. */
    public static final int SLOT_MINUTES = 30;

    private final DepartmentService departmentService = new DepartmentService();

    /** Free consultation slots for a doctor on a date, derived from the published roster. */
    public List<Slot> availableSlots(String doctorId, LocalDate date) {
        List<Slot> slots = new ArrayList<>();
        List<Shift> shifts = Database.shifts.stream()
                .filter(s -> s.getDoctorId().equalsIgnoreCase(doctorId)
                        && s.getDate().equals(date)
                        && s.getStatus() == ShiftStatus.SCHEDULED)
                .collect(Collectors.toList());
        for (Shift shift : shifts) {
            LocalTime t = shift.getStart();
            while (!t.plusMinutes(SLOT_MINUTES).isAfter(shift.getEnd())) {
                LocalTime end = t.plusMinutes(SLOT_MINUTES);
                if (isFree(doctorId, date, t)) slots.add(new Slot(doctorId, date, t, end));
                t = end;
            }
        }
        slots.sort(Comparator.comparing(Slot::getStart));
        return slots;
    }

    public Appointment book(String patientId, String doctorId, LocalDate date, LocalTime start) {
        requirePatient(patientId);
        Doctor doctor = requireDoctor(doctorId);
        if (availableSlots(doctorId, date).stream().noneMatch(s -> s.getStart().equals(start)))
            throw new IllegalArgumentException("That slot is not available - check the doctor's open slots");

        String id = IdGenerator.next("APT", Database.appointments.stream()
                .map(Appointment::getAppointmentId).collect(Collectors.toList()));
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
        boolean free = sameSlot || availableSlots(appt.getDoctorId(), date).stream()
                .anyMatch(s -> s.getStart().equals(start));
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
        return Database.appointments.stream()
                .filter(a -> a.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .collect(Collectors.toList());
    }

    public List<Appointment> forDoctor(String doctorId) {
        return Database.appointments.stream()
                .filter(a -> a.getDoctorId().equalsIgnoreCase(doctorId))
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .collect(Collectors.toList());
    }

    // ----- helpers -----

    private boolean isFree(String doctorId, LocalDate date, LocalTime start) {
        LocalDateTime at = LocalDateTime.of(date, start);
        return Database.appointments.stream().noneMatch(a ->
                a.getDoctorId().equalsIgnoreCase(doctorId)
                        && a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getDateTime().equals(at));
    }

    private double feeFor(Doctor doctor) {
        if (doctor.getDepartmentCode() != null) {
            return departmentService.findByCode(doctor.getDepartmentCode())
                    .map(Department::getConsultationFee)
                    .filter(fee -> fee > 0)
                    .orElse(Database.clinicConfig.getBaseConsultationRate());
        }
        return Database.clinicConfig.getBaseConsultationRate();
    }

    private void requirePatient(String id) {
        String key = (id == null) ? "" : id.trim();
        if (Database.patients.stream().noneMatch(p -> p.getId().equalsIgnoreCase(key)))
            throw new IllegalArgumentException("No such patient: " + id);
    }

    private Doctor requireDoctor(String id) {
        String key = (id == null) ? "" : id.trim();
        return Database.doctors.stream()
                .filter(d -> d.getId().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such doctor: " + id));
    }

    private Appointment require(String id) {
        String key = (id == null) ? "" : id.trim();
        return Database.appointments.stream()
                .filter(a -> a.getAppointmentId().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such appointment: " + id));
    }
}
