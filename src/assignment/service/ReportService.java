package assignment.service;

import assignment.model.Appointment;
import assignment.model.AppointmentStatus;
import assignment.model.Department;
import assignment.model.Invoice;
import assignment.model.PaymentStatus;
import assignment.model.ShiftRoster;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Use-case: view analytical reports on hospital metrics and revenue summaries. */
public class ReportService {

    private final DepartmentService departmentService = new DepartmentService();

    /** Operational snapshot: departments, staffing, appointment outcomes, roster coverage. */
    public String hospitalMetricsReport() {
        StringBuilder sb = new StringBuilder();
        line(sb);
        sb.append("        APU MEDICAL CENTRE  -  HOSPITAL METRICS REPORT\n");
        line(sb);

        long activeDept = Database.departments.stream().filter(Department::isActive).count();
        sb.append(String.format("Departments        : %d  (%d active)%n", Database.departments.size(), activeDept));
        sb.append(String.format("Doctors            : %d%n", Database.doctors.size()));
        sb.append(String.format("Patients           : %d%n", Database.patients.size()));
        sb.append(String.format("Appointments       : %d%n", Database.appointments.size()));
        sb.append(String.format("Rosters            : %d%n", Database.rosters.size()));
        sb.append(String.format("Rostered shifts    : %d%n", Database.shifts.size()));

        sb.append("\n-- Doctors per department --\n");
        for (Department d : Database.departments) {
            long docs = Database.doctors.stream()
                    .filter(x -> d.getCode().equalsIgnoreCase(x.getDepartmentCode()))
                    .count();
            sb.append(String.format("  %-6s %-20s %d doctor(s)%n", d.getCode(), d.getName(), docs));
        }

        sb.append("\n-- Appointments by status --\n");
        Map<AppointmentStatus, Long> byStatus = Database.appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        for (AppointmentStatus s : AppointmentStatus.values()) {
            sb.append(String.format("  %-12s %d%n", s, byStatus.getOrDefault(s, 0L)));
        }

        sb.append("\n-- Rostered shifts per department --\n");
        for (Department d : Database.departments) {
            List<String> rosterIds = Database.rosters.stream()
                    .filter(r -> r.getDepartmentCode().equalsIgnoreCase(d.getCode()))
                    .map(ShiftRoster::getRosterId)
                    .collect(Collectors.toList());
            long sh = Database.shifts.stream()
                    .filter(x -> rosterIds.contains(x.getRosterId()))
                    .count();
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

        double billed = Database.invoices.stream().mapToDouble(Invoice::getAmount).sum();
        double collected = Database.invoices.stream()
                .filter(i -> i.getStatus() == PaymentStatus.PAID)
                .mapToDouble(Invoice::getAmount).sum();
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
            byDept.merge(key, i.getAmount(), Double::sum);
        }
        for (Map.Entry<String, Double> e : byDept.entrySet()) {
            String name = departmentService.findByCode(e.getKey())
                    .map(Department::getName).orElse(e.getKey());
            sb.append(String.format("  %-6s %-20s RM %,.2f%n", e.getKey(), name, e.getValue()));
        }

        sb.append("\n-- Revenue by month (billed) --\n");
        Map<YearMonth, Double> byMonth = new TreeMap<>();
        for (Invoice i : Database.invoices) {
            byMonth.merge(YearMonth.from(i.getIssueDate()), i.getAmount(), Double::sum);
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
