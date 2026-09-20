package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;

/** A billing document raised against an appointment. Source data for revenue reports. */
public class Invoice {

    private String invoiceId;
    private String patientId;
    private String appointmentId;
    private String departmentCode;
    private LocalDate issueDate;
    private double amount;
    private PaymentStatus status;

    public Invoice() { }

    public Invoice(String invoiceId, String patientId, String appointmentId, String departmentCode,
                   LocalDate issueDate, double amount, PaymentStatus status) {
        this.invoiceId = invoiceId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.departmentCode = departmentCode;
        this.issueDate = issueDate;
        this.amount = amount;
        this.status = status;
    }

    public String getInvoiceId() { return invoiceId; }
    public String getPatientId() { return patientId; }
    public String getAppointmentId() { return appointmentId; }
    public String getDepartmentCode() { return departmentCode; }
    public LocalDate getIssueDate() { return issueDate; }
    public double getAmount() { return amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String toCsv() {
        return CsvUtil.join(invoiceId, patientId, appointmentId, departmentCode,
                issueDate, amount, status);
    }

    public static Invoice fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Invoice(f[0], f[1], f[2], f[3], LocalDate.parse(f[4]),
                f[5].isBlank() ? 0.0 : Double.parseDouble(f[5]),
                PaymentStatus.fromString(f[6]));
    }
}
