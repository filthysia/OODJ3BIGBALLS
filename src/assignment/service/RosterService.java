package assignment.service;

import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.ShiftStatus;
import assignment.model.ShiftType;
import assignment.util.IdGenerator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/** Use-case: design and modify operational weekly shift rosters for doctors. */
public class RosterService {

    private final DepartmentService departmentService = new DepartmentService();

    public List<ShiftRoster> getAll() {
        return Database.rosters;
    }

    public List<ShiftRoster> getByDepartment(String code) {
        List<ShiftRoster> result = new ArrayList<>();
        for (ShiftRoster r : Database.rosters) {
            if (r.getDepartmentCode().equalsIgnoreCase(code)) result.add(r);
        }
        return result;
    }

    public ShiftRoster findById(String rosterId) {
        for (ShiftRoster r : Database.rosters) {
            if (r.getRosterId().equalsIgnoreCase(rosterId)) return r;
        }
        return null;
    }

    /**
     * Creates an empty weekly roster. {@code anyDateInWeek} is snapped back to the
     * Monday of that week. Only one roster per department per week is allowed.
     */
    public ShiftRoster createRoster(String departmentCode, LocalDate anyDateInWeek, String managerId) {
        Department d = departmentService.findByCode(departmentCode);
        if (d == null) throw new IllegalArgumentException("No such department: " + departmentCode);
        LocalDate weekStart = anyDateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        boolean exists = false;
        for (ShiftRoster r : Database.rosters) {
            if (r.getDepartmentCode().equalsIgnoreCase(d.getCode()) && r.getWeekStart().equals(weekStart)) {
                exists = true;
                break;
            }
        }
        if (exists)
            throw new IllegalArgumentException("A roster for " + d.getCode() + " week " + weekStart + " already exists");

        List<String> existingIds = new ArrayList<>();
        for (ShiftRoster r : Database.rosters) existingIds.add(r.getRosterId());
        String id = IdGenerator.next("RST", existingIds);

        ShiftRoster roster = new ShiftRoster(id, d.getCode(), weekStart, managerId);
        Database.rosters.add(roster);
        Database.saveRostersAndShifts();
        return roster;
    }

    /** Adds a standard shift band for a doctor, rejecting clashes with their other shifts. */
    public Shift addShift(String rosterId, String doctorId, LocalDate date, ShiftType type) {
        ShiftRoster roster = requireRoster(rosterId);
        requireDoctorInDepartment(roster.getDepartmentCode(), doctorId);
        requireDateInWeek(roster, date);

        List<String> existingIds = new ArrayList<>();
        for (Shift s : Database.shifts) existingIds.add(s.getShiftId());
        String id = IdGenerator.next("SH", existingIds);

        Shift shift = new Shift(id, rosterId, doctorId, date, type,
                type.getDefaultStart(), type.getDefaultEnd(), ShiftStatus.SCHEDULED);

        Shift clash = roster.findClash(shift);
        if (clash != null)
            throw new IllegalStateException("Doctor already has shift " + clash.getShiftId()
                    + " (" + clash.getType() + ") on " + clash.getDate());

        Database.shifts.add(shift);
        roster.getShifts().add(shift);
        Database.saveRostersAndShifts();
        return shift;
    }

    /**
     * Modifies an existing shift. Pass {@code null} for any parameter to keep the
     * current value. Changing the type resets start/end to that band's defaults
     * unless explicit times are supplied.
     */
    public Shift updateShift(String shiftId, LocalDate newDate, ShiftType newType,
                             LocalTime newStart, LocalTime newEnd, ShiftStatus newStatus) {
        Shift shift = requireShift(shiftId);
        ShiftRoster roster = requireRoster(shift.getRosterId());

        LocalDate date = newDate != null ? newDate : shift.getDate();
        ShiftType type = newType != null ? newType : shift.getType();
        LocalTime start = newStart != null ? newStart
                : (newType != null ? newType.getDefaultStart() : shift.getStart());
        LocalTime end = newEnd != null ? newEnd
                : (newType != null ? newType.getDefaultEnd() : shift.getEnd());

        requireDateInWeek(roster, date);
        if (!start.isBefore(end))
            throw new IllegalArgumentException("Shift start must be before end");

        Shift probe = new Shift(shift.getShiftId(), shift.getRosterId(), shift.getDoctorId(),
                date, type, start, end, shift.getStatus());
        Shift clash = roster.findClash(probe);
        if (clash != null)
            throw new IllegalStateException("Change clashes with shift " + clash.getShiftId()
                    + " on " + clash.getDate());

        shift.setDate(date);
        shift.setType(type);
        shift.setStart(start);
        shift.setEnd(end);
        if (newStatus != null) shift.setStatus(newStatus);
        Database.saveRostersAndShifts();
        return shift;
    }

    public void removeShift(String shiftId) {
        Shift shift = requireShift(shiftId);
        for (int i = 0; i < Database.shifts.size(); i++) {
            if (Database.shifts.get(i).getShiftId().equals(shiftId)) {
                Database.shifts.remove(i);
                break;
            }
        }
        ShiftRoster roster = findById(shift.getRosterId());
        if (roster != null) roster.removeShift(shiftId);
        Database.saveRostersAndShifts();
    }

    /** Shifts of one roster, ordered by date then start time. */
    public List<Shift> shiftsFor(String rosterId) {
        List<Shift> result = new ArrayList<>();
        for (Shift s : Database.shifts) {
            if (s.getRosterId().equalsIgnoreCase(rosterId)) result.add(s);
        }
        // insertion sort by date then start time - one roster's shift list is short
        for (int i = 1; i < result.size(); i++) {
            Shift current = result.get(i);
            int j = i - 1;
            while (j >= 0 && isAfter(result.get(j), current)) {
                result.set(j + 1, result.get(j));
                j--;
            }
            result.set(j + 1, current);
        }
        return result;
    }

    private boolean isAfter(Shift a, Shift b) {
        if (!a.getDate().equals(b.getDate())) return a.getDate().isAfter(b.getDate());
        return a.getStart().isAfter(b.getStart());
    }

    // ---- internal helpers ----

    private ShiftRoster requireRoster(String rosterId) {
        ShiftRoster r = findById(rosterId);
        if (r == null) throw new IllegalArgumentException("No such roster: " + rosterId);
        return r;
    }

    private Shift requireShift(String shiftId) {
        String key = (shiftId == null) ? "" : shiftId.trim();
        for (Shift s : Database.shifts) {
            if (s.getShiftId().equalsIgnoreCase(key)) return s;
        }
        throw new IllegalArgumentException("No such shift: " + shiftId);
    }

    private void requireDateInWeek(ShiftRoster roster, LocalDate date) {
        if (date.isBefore(roster.getWeekStart()) || date.isAfter(roster.getWeekEnd())) {
            throw new IllegalArgumentException("Date " + date + " is outside the roster week "
                    + roster.getWeekStart() + ".." + roster.getWeekEnd());
        }
    }

    private void requireDoctorInDepartment(String deptCode, String doctorId) {
        Department d = departmentService.findByCode(deptCode);
        if (d == null) throw new IllegalArgumentException("No such department: " + deptCode);

        boolean assigned = d.getDoctorIds().contains(doctorId);
        if (!assigned) {
            for (Doctor x : Database.doctors) {
                if (x.getId().equals(doctorId) && deptCode.equalsIgnoreCase(x.getDepartmentCode())) {
                    assigned = true;
                    break;
                }
            }
        }
        if (!assigned)
            throw new IllegalArgumentException("Doctor " + doctorId + " is not assigned to " + deptCode);
    }
}
