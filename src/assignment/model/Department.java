package assignment.model;

import assignment.util.CsvUtil;
import java.util.ArrayList;
import java.util.List;

/** A specialised clinical department, e.g. Cardiology. */
public class Department {

    private String code;            // short unique code, e.g. CARD
    private String name;            // e.g. Cardiology
    private String description;
    private String headDoctorId;    // nullable
    private double consultationFee;
    private DepartmentStatus status;
    private final List<String> doctorIds = new ArrayList<>();

    public Department() {
        this.status = DepartmentStatus.ACTIVE;
    }

    public Department(String code, String name, String description,
                      String headDoctorId, double consultationFee, DepartmentStatus status) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.headDoctorId = headDoctorId;
        this.consultationFee = consultationFee;
        this.status = (status == null) ? DepartmentStatus.ACTIVE : status;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHeadDoctorId() { return headDoctorId; }
    public void setHeadDoctorId(String headDoctorId) { this.headDoctorId = headDoctorId; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    public DepartmentStatus getStatus() { return status; }
    public void setStatus(DepartmentStatus status) { this.status = status; }

    public List<String> getDoctorIds() { return doctorIds; }

    public boolean isActive() { return status == DepartmentStatus.ACTIVE; }

    public void addDoctor(String doctorId) {
        if (doctorId != null && !doctorIds.contains(doctorId)) doctorIds.add(doctorId);
    }

    public void removeDoctor(String doctorId) {
        doctorIds.remove(doctorId);
        if (doctorId != null && doctorId.equals(headDoctorId)) headDoctorId = null;
    }

    public String toCsv() {
        return CsvUtil.join(code, name, description, headDoctorId,
                consultationFee, status, CsvUtil.joinList(doctorIds));
    }

    public static Department fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        Department d = new Department(f[0], f[1], f[2],
                f[3].isBlank() ? null : f[3],
                f[4].isBlank() ? 0.0 : Double.parseDouble(f[4]),
                DepartmentStatus.fromString(f[5]));
        if (f.length > 6) d.doctorIds.addAll(CsvUtil.splitList(f[6]));
        return d;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-16s fee=RM%-8.2f %-8s doctors=%d%s",
                code, name, consultationFee, status, doctorIds.size(),
                headDoctorId == null ? "" : "  head=" + headDoctorId);
    }
}
