package assignment.service;

import assignment.model.Appointment;
import assignment.model.AppointmentStatus;
import assignment.model.Department;
import assignment.model.Doctor;
import assignment.model.Invoice;
import assignment.model.PaymentStatus;
import assignment.model.Shift;
import assignment.model.ShiftRoster;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Use-case: view analytical reports on hospital metrics and revenue summaries. */
public class ReportService {

    private final DepartmentService departmentService = new DepartmentService();

    /** Operational snapshot: departments, staffing, appointment outcomes, roster coverage. */
    public String hospitalMetricsReport() {
        StringBuilder sb = new StringBuilder();
        line(sb);
        sb.append("        APU MEDICAL CENTRE  -  HOSPITAL METRICS REPORT\n");
        line(sb);

        int activeDept = 0;
        for (Department d : Database.departments) {
            if (d.isActive()) activeDept++;
        }
        sb.append(String.format("Departments        : %d  (%d active)%n", Database.departments.size(), activeDept));
        sb.append(String.format("Doctors            : %d%n", Database.doctors.size()));
        sb.append(String.format("Patients           : %d%n", Database.patients.size()));
        sb.append(String.format("Appointments       : %d%n", Database.appointments.size()));
        sb.append(String.format("Rosters            : %d%n", Database.rosters.size()));
        sb.append(String.format("Rostered shifts    : %d%n", Database.shifts.size()));

        sb.append("\n-- Doctors per department --\n");
        for (Department d : Database.departments) {
            int docs = 0;
            for (Doctor x : Database.doctors) {
                if (d.getCode().equalsIgnoreCase(x.getDepartmentCode())) docs++;
            }
            sb.append(String.format("  %-6s %-20s %d doctor(s)%n", d.getCode(), d.getName(), docs));
        }

        sb.append("\n-- Appointments by status --\n");
        for (AppointmentStatus s : AppointmentStatus.values()) {
            int count = 0;
            for (Appointment a : Database.appointments) {
                if (a.getStatus() == s) count++;
            }
            sb.append(String.format("  %-12s %d%n", s, count));
        }

        sb.append("\n-- Rostered shifts per department --\n");
        for (Department d : Database.departments) {
            List<String> rosterIds = new ArrayList<>();
            for (ShiftRoster r : Database.rosters) {
                if (r.getDepartmentCode().equalsIgnoreCase(d.getCode())) rosterIds.add(r.getRosterId());
            }
            int sh = 0;
            for (Shift x : Database.shifts) {
                if (rosterIds.contains(x.getRosterId())) sh++;
            }
            sb.append(String.format("  %-6s %-20s %d shift(s)%n", d.getCode(), d.getName(), sh));
        }

        line(sb);
        return sb.toString();
    }

    /** Financial snapshot: billed vs collected vs outstanding, broken down by department and month. */
    public String revenueSummaryReport() {
        StringBuilder sb = new StringBuilder();
        line(sb);
        sb.append("        APU MEDICAL CENTRE  -  REVENUE SUMMARY REPORT\n");
        line(sb);

        double billed = 0;
        double collected = 0;
        for (Invoice i : Database.invoices) {
            billed += i.getAmount();
            if (i.getStatus() == PaymentStatus.PAID) collected += i.getAmount();
        }
        double outstanding = billed - collected;

        sb.append(String.format("Invoices issued    : %d%n", Database.invoices.size()));
        sb.append(String.format("Total billed       : RM %,.2f%n", billed));
        sb.append(String.format("Total collected    : RM %,.2f%n", collected));
        sb.append(String.format("Outstanding        : RM %,.2f%n", outstanding));
        if (!Database.invoices.isEmpty()) {
            sb.append(String.format("Average invoice    : RM %,.2f%n", billed / Database.invoices.size()));
        }

        sb.append("\n-- Revenue by department (billed) --\n");
        Map<String, Double> byDept = new TreeMap<>();
        for (Invoice i : Database.invoices) {
            String key = (i.getDepartmentCode() == null || i.getDepartmentCode().isBlank())
                    ? "-" : i.getDepartmentCode();
            if (byDept.containsKey(key)) {
                byDept.put(key, byDept.get(key) + i.getAmount());
            } else {
                byDept.put(key, i.getAmount());
            }
        }
        for (Map.Entry<String, Double> e : byDept.entrySet()) {
            Department dep = departmentService.findByCode(e.getKey());
            String name = (dep != null) ? dep.getName() : e.getKey();
            sb.append(String.format("  %-6s %-20s RM %,.2f%n", e.getKey(), name, e.getValue()));
        }

        sb.append("\n-- Revenue by month (billed) --\n");
        Map<YearMonth, Double> byMonth = new TreeMap<>();
        for (Invoice i : Database.invoices) {
            YearMonth ym = YearMonth.from(i.getIssueDate());
            if (byMonth.containsKey(ym)) {
                byMonth.put(ym, byMonth.get(ym) + i.getAmount());
            } else {
                byMonth.put(ym, i.getAmount());
            }
        }
        for (Map.Entry<YearMonth, Double> e : byMonth.entrySet()) {
            sb.append(String.format("  %s    RM %,.2f%n", e.getKey(), e.getValue()));
        }

        line(sb);
        return sb.toString();
    }

    private void line(StringBuilder sb) {
        sb.append("=====================================================\n");
    }
}
