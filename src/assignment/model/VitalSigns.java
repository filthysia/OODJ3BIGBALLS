package assignment.model;

import assignment.util.CsvUtil;
import java.time.LocalDateTime;

/** A single set of patient vital-sign readings logged by a doctor during a consultation. */
public class VitalSigns {

    private String recordId;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private LocalDateTime recordedAt;
    private double temperatureC;
    private int systolic;
    private int diastolic;
    private int heartRate;
    private int respiratoryRate;
    private int spo2;
    private double weightKg;
    private double heightCm;

    public VitalSigns() { }

    public VitalSigns(String recordId, String patientId, String doctorId, String appointmentId,
                      LocalDateTime recordedAt, double temperatureC, int systolic, int diastolic,
                      int heartRate, int respiratoryRate, int spo2, double weightKg, double heightCm) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.recordedAt = recordedAt;
        this.temperatureC = temperatureC;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.spo2 = spo2;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    public String getRecordId() { return recordId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public double getTemperatureC() { return temperatureC; }
    public int getSystolic() { return systolic; }
    public int getDiastolic() { return diastolic; }
    public int getHeartRate() { return heartRate; }
    public int getRespiratoryRate() { return respiratoryRate; }
    public int getSpo2() { return spo2; }
    public double getWeightKg() { return weightKg; }
    public double getHeightCm() { return heightCm; }

    /** Body-mass index, or 0 when height is unknown. */
    public double bmi() {
        if (heightCm <= 0) return 0.0;
        double m = heightCm / 100.0;
        return weightKg / (m * m);
    }

    public String toCsv() {
        return CsvUtil.join(recordId, patientId, doctorId, appointmentId, recordedAt,
                temperatureC, systolic, diastolic, heartRate, respiratoryRate, spo2, weightKg, heightCm);
    }

    public static VitalSigns fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new VitalSigns(f[0], f[1], f[2], f[3], LocalDateTime.parse(f[4]),
                d(f[5]), i(f[6]), i(f[7]), i(f[8]), i(f[9]), i(f[10]), d(f[11]), d(f[12]));
    }

    private static double d(String s) { return s == null || s.isBlank() ? 0.0 : Double.parseDouble(s); }
    private static int i(String s) { return s == null || s.isBlank() ? 0 : Integer.parseInt(s); }

    @Override
    public String toString() {
        return String.format("%s  T=%.1fC BP=%d/%d HR=%d RR=%d SpO2=%d%%  W=%.1fkg",
                recordedAt, temperatureC, systolic, diastolic, heartRate, respiratoryRate, spo2, weightKg);
    }
}
