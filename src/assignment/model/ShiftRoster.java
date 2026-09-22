package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A weekly operational shift roster for one department.
 *
 * <p>The roster header is persisted on its own; the {@link Shift} rows are
 * persisted separately and linked back by {@code rosterId}. {@link #getShifts()}
 * is populated in memory for convenience.</p>
 */
public class ShiftRoster {

    private String rosterId;
    private String departmentCode;
    private LocalDate weekStart;      // always a Monday
    private String createdBy;         // medical manager id
    private final List<Shift> shifts = new ArrayList<>();

    public ShiftRoster() { }

    public ShiftRoster(String rosterId, String departmentCode, LocalDate weekStart, String createdBy) {
        this.rosterId = rosterId;
        this.departmentCode = departmentCode;
        this.weekStart = weekStart;
        this.createdBy = createdBy;
    }

    public String getRosterId() { return rosterId; }
    public void setRosterId(String rosterId) { this.rosterId = rosterId; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public LocalDate getWeekStart() { return weekStart; }
    public void setWeekStart(LocalDate weekStart) { this.weekStart = weekStart; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<Shift> getShifts() { return shifts; }

    public LocalDate getWeekEnd() {
        return weekStart == null ? null : weekStart.plusDays(6);
    }

    /** Returns an existing shift for the same doctor whose time clashes with {@code candidate}. */
    public Shift findClash(Shift candidate) {
        for (Shift s : shifts) {
            if (s.getShiftId().equals(candidate.getShiftId())) continue;
            if (s.getDoctorId().equals(candidate.getDoctorId()) && s.overlaps(candidate)) {
                return s;
            }
        }
        return null;
    }

    public Shift findShift(String shiftId) {
        for (Shift s : shifts) {
            if (s.getShiftId().equals(shiftId)) return s;
        }
        return null;
    }

    public boolean removeShift(String shiftId) {
        for (int i = 0; i < shifts.size(); i++) {
            if (shifts.get(i).getShiftId().equals(shiftId)) {
                shifts.remove(i);
                return true;
            }
        }
        return false;
    }

    public String toCsv() {
        return CsvUtil.join(rosterId, departmentCode, weekStart, createdBy);
    }

    public static ShiftRoster fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new ShiftRoster(f[0], f[1], LocalDate.parse(f[2]), f[3]);
    }

    @Override
    public String toString() {
        return String.format("%-9s dept=%-6s week %s..%s  shifts=%d  by=%s",
                rosterId, departmentCode, weekStart, getWeekEnd(), shifts.size(), createdBy);
    }
}
