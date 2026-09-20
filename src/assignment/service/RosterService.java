package assignment.service;

import assignment.model.Department;
import assignment.model.Shift;
import assignment.model.ShiftRoster;
import assignment.model.ShiftStatus;
import assignment.model.ShiftType;
import assignment.util.IdGenerator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Use-case: design and modify operational weekly shift rosters for doctors. */
public class RosterService {

    private final DepartmentService departmentService = new DepartmentService();

    public List<ShiftRoster> getAll() {
        return Database.rosters;
    }

    public List<ShiftRoster> getByDepartment(String code) {
        return Database.rosters.stream()
                .filter(r -> r.getDepartmentCode().equalsIgnoreCase(code))
                .collect(Collectors.toList());
    }

    public ShiftRoster findById(String rosterId) {
        return Database.rosters.stream()
                .filter(r -> r.getRosterId().equalsIgnoreCase(rosterId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates an empty weekly roster. {@code anyDateInWeek} is snapped back to the
     * Monday of that week. Only one roster per department per week is allowed.
     */
    public ShiftRoster createRoster(String departmentCode, LocalDate anyDateInWeek, String managerId) {
        Department d = departmentService.findByCode(departmentCode)
                .orElseThrow(() -> new IllegalArgumentException("No such department: " + departmentCode));
        LocalDate weekStart = anyDateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        boolean exists = Database.rosters.stream().anyMatch(r ->
                r.getDepartmentCode().equalsIgnoreCase(d.getCode()) && r.getWeekStart().equals(weekStart));
        if (exists)
            throw new IllegalArgumentException("A roster for " + d.getCode() + " week " + weekStart + " already exists");

        String id = IdGenerator.next("RST", Database.rosters.stream()
                .map(ShiftRoster::getRosterId).collect(Collectors.toList()));
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

        String id = IdGenerator.next("SH", Database.shifts.stream()
                .map(Shift::getShiftId).collect(Collectors.toList()));
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
        Database.shifts.removeIf(s -> s.getShiftId().equals(shiftId));
        ShiftRoster roster = findById(shift.getRosterId());
        if (roster != null) roster.removeShift(shiftId);
        Database.saveRostersAndShifts();
    }

    /** Shifts of one roster, ordered by date then start time. */
    public List<Shift> shiftsFor(String rosterId) {
        return Database.shifts.stream()
                .filter(s -> s.getRosterId().equalsIgnoreCase(rosterId))
                .sorted(Comparator.comparing(Shift::getDate).thenComparing(Shift::getStart))
                .collect(Collectors.toList());
    }

    // ---- internal helpers ----

    private ShiftRoster requireRoster(String rosterId) {
        ShiftRoster r = findById(rosterId);
        if (r == null) throw new IllegalArgumentException("No such roster: " + rosterId);
        return r;
    }

    private Shift requireShift(String shiftId) {
        return Database.shifts.stream()
                .filter(s -> s.getShiftId().equalsIgnoreCase(shiftId == null ? "" : shiftId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such shift: " + shiftId));
    }

    private void requireDateInWeek(ShiftRoster roster, LocalDate date) {
        if (date.isBefore(roster.getWeekStart()) || date.isAfter(roster.getWeekEnd())) {
            throw new IllegalArgumentException("Date " + date + " is outside the roster week "
                    + roster.getWeekStart() + ".." + roster.getWeekEnd());
        }
    }

    private void requireDoctorInDepartment(String deptCode, String doctorId) {
        Department d = departmentService.findByCode(deptCode)
                .orElseThrow(() -> new IllegalArgumentException("No such department: " + deptCode));
        boolean assigned = d.getDoctorIds().contains(doctorId)
                || Database.doctors.stream().anyMatch(x -> x.getId().equals(doctorId)
                        && deptCode.equalsIgnoreCase(x.getDepartmentCode()));
        if (!assigned)
            throw new IllegalArgumentException("Doctor " + doctorId + " is not assigned to " + deptCode);
    }
}
