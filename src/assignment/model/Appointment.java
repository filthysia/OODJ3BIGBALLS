package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDateTime;

/** A patient consultation slot. Source data for hospital metric reports. */
public class Appointment {

    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String departmentCode;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private double consultationFee;

    public Appointment() { }

    public Appointment(String appointmentId, String patientId, String doctorId, String departmentCode,
                       LocalDateTime dateTime, AppointmentStatus status, double consultationFee) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.departmentCode = departmentCode;
        this.dateTime = dateTime;
        this.status = status;
        this.consultationFee = consultationFee;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public String toCsv() {
        return CsvUtil.join(appointmentId, patientId, doctorId, departmentCode,
                dateTime, status, consultationFee);
    }

    public static Appointment fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Appointment(f[0], f[1], f[2], f[3], LocalDateTime.parse(f[4]),
                AppointmentStatus.fromString(f[5]),
                f[6].isBlank() ? 0.0 : Double.parseDouble(f[6]));
    }

    @Override
    public String toString() {
        return String.format("%-9s %s  patient=%-8s doctor=%-8s %-6s %-10s RM%.2f",
                appointmentId, dateTime, patientId, doctorId, departmentCode, status, consultationFee);
    }
}
