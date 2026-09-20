package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A digital prescription attached to a patient's record. The header is stored in
 * prescriptions.txt; the {@link PrescriptionItem} rows are stored separately and
 * linked by {@code prescriptionId}. {@link #getItems()} is filled in memory.
 */
public class Prescription {

    private String prescriptionId;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private LocalDate issuedDate;
    private PrescriptionStatus status;
    private final List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {
        this.status = PrescriptionStatus.ACTIVE;
    }

    public Prescription(String prescriptionId, String patientId, String doctorId, String appointmentId,
                        LocalDate issuedDate, PrescriptionStatus status) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.issuedDate = issuedDate;
        this.status = (status == null) ? PrescriptionStatus.ACTIVE : status;
    }

    public String getPrescriptionId() { return prescriptionId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public LocalDate getIssuedDate() { return issuedDate; }

    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }

    public List<PrescriptionItem> getItems() { return items; }

    public String toCsv() {
        return CsvUtil.join(prescriptionId, patientId, doctorId, appointmentId, issuedDate, status);
    }

    public static Prescription fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Prescription(f[0], f[1], f[2],
                f[3].isBlank() ? null : f[3],
                LocalDate.parse(f[4]),
                PrescriptionStatus.fromString(f[5]));
    }

    @Override
    public String toString() {
        return String.format("%-9s %s  %-9s  items=%d  (Dr %s)",
                prescriptionId, issuedDate, status, items.size(), doctorId);
    }
}
