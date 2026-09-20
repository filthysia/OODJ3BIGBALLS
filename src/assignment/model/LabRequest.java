package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;

/**
 * A doctor's request to Admin Staff for a lab test, X-ray or specialised imaging.
 * Admin Staff schedules it onto a matching {@link HospitalAsset} and later records the result.
 */
public class LabRequest {

    private String requestId;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private LabTestType type;
    private LocalDate requestedDate;
    private LabRequestStatus status;
    private String assignedAssetId;   // set by Admin Staff when scheduled
    private String clinicalReason;
    private String resultNotes;       // set by Admin Staff when completed

    public LabRequest() {
        this.status = LabRequestStatus.REQUESTED;
    }

    public LabRequest(String requestId, String patientId, String doctorId, String appointmentId,
                      LabTestType type, LocalDate requestedDate, LabRequestStatus status,
                      String assignedAssetId, String clinicalReason, String resultNotes) {
        this.requestId = requestId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.type = type;
        this.requestedDate = requestedDate;
        this.status = (status == null) ? LabRequestStatus.REQUESTED : status;
        this.assignedAssetId = assignedAssetId;
        this.clinicalReason = clinicalReason;
        this.resultNotes = resultNotes;
    }

    public String getRequestId() { return requestId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public LabTestType getType() { return type; }
    public LocalDate getRequestedDate() { return requestedDate; }

    public LabRequestStatus getStatus() { return status; }
    public void setStatus(LabRequestStatus status) { this.status = status; }

    public String getAssignedAssetId() { return assignedAssetId; }
    public void setAssignedAssetId(String assignedAssetId) { this.assignedAssetId = assignedAssetId; }

    public String getClinicalReason() { return clinicalReason; }
    public void setClinicalReason(String clinicalReason) { this.clinicalReason = clinicalReason; }

    public String getResultNotes() { return resultNotes; }
    public void setResultNotes(String resultNotes) { this.resultNotes = resultNotes; }

    public String toCsv() {
        return CsvUtil.join(requestId, patientId, doctorId, appointmentId, type, requestedDate,
                status, assignedAssetId, clinicalReason, resultNotes);
    }

    public static LabRequest fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new LabRequest(f[0], f[1], f[2],
                f[3].isBlank() ? null : f[3],
                LabTestType.fromString(f[4]),
                LocalDate.parse(f[5]),
                LabRequestStatus.fromString(f[6]),
                (f.length > 7 && !f[7].isBlank()) ? f[7] : null,
                f.length > 8 ? f[8] : "",
                f.length > 9 ? f[9] : "");
    }

    @Override
    public String toString() {
        return String.format("%-9s %-12s %s  %-10s %s%s",
                requestId, type, requestedDate, status,
                assignedAssetId == null ? "" : "asset=" + assignedAssetId,
                resultNotes == null || resultNotes.isBlank() ? "" : "  result: " + resultNotes);
    }
}
