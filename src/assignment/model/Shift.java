package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;
import java.time.LocalTime;

/** A single rostered working period for one doctor on one day. */
public class Shift {

    private String shiftId;
    private String rosterId;
    private String doctorId;
    private LocalDate date;
    private ShiftType type;
    private LocalTime start;
    private LocalTime end;
    private ShiftStatus status;

    public Shift() { }

    public Shift(String shiftId, String rosterId, String doctorId, LocalDate date,
                 ShiftType type, LocalTime start, LocalTime end, ShiftStatus status) {
        this.shiftId = shiftId;
        this.rosterId = rosterId;
        this.doctorId = doctorId;
        this.date = date;
        this.type = type;
        this.start = start;
        this.end = end;
        this.status = (status == null) ? ShiftStatus.SCHEDULED : status;
    }

    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }

    public String getRosterId() { return rosterId; }
    public void setRosterId(String rosterId) { this.rosterId = rosterId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public ShiftType getType() { return type; }
    public void setType(ShiftType type) { this.type = type; }

    public LocalTime getStart() { return start; }
    public void setStart(LocalTime start) { this.start = start; }

    public LocalTime getEnd() { return end; }
    public void setEnd(LocalTime end) { this.end = end; }

    public ShiftStatus getStatus() { return status; }
    public void setStatus(ShiftStatus status) { this.status = status; }

    /** True when this shift is on the same day and its time range overlaps {@code other}. */
    public boolean overlaps(Shift other) {
        return date.equals(other.date)
                && start.isBefore(other.end)
                && other.start.isBefore(end);
    }

    public String toCsv() {
        return CsvUtil.join(shiftId, rosterId, doctorId, date, type, start, end, status);
    }

    public static Shift fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new Shift(f[0], f[1], f[2], LocalDate.parse(f[3]), ShiftType.fromString(f[4]),
                LocalTime.parse(f[5]), LocalTime.parse(f[6]), ShiftStatus.fromString(f[7]));
    }

    @Override
    public String toString() {
        return String.format("%-9s %s  %-9s %s-%s  %-9s doctor=%s",
                shiftId, date, type, start, end, status, doctorId);
    }
}
