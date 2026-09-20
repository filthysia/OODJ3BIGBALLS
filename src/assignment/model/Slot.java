package assignment.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** A bookable consultation window derived from a doctor's rostered shift. Not persisted. */
public class Slot {

    private final String doctorId;
    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;

    public Slot(String doctorId, LocalDate date, LocalTime start, LocalTime end) {
        this.doctorId = doctorId;
        this.date = date;
        this.start = start;
        this.end = end;
    }

    public String getDoctorId() { return doctorId; }
    public LocalDate getDate() { return date; }
    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }

    public LocalDateTime startDateTime() { return LocalDateTime.of(date, start); }

    @Override
    public String toString() {
        return date + " " + start + "-" + end;
    }
}
