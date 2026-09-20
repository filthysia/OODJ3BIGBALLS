package assignment.model;

import assignment.util.CsvUtil;

/** One medication line on a prescription. Stored in prescription_items.txt, linked by prescriptionId. */
public class PrescriptionItem {

    private String itemId;
    private String prescriptionId;
    private String drugName;
    private String dosage;        // e.g. "500 mg"
    private String frequency;     // e.g. "TDS"
    private int durationDays;
    private int quantity;
    private String instructions;

    public PrescriptionItem() { }

    public PrescriptionItem(String itemId, String prescriptionId, String drugName, String dosage,
                            String frequency, int durationDays, int quantity, String instructions) {
        this.itemId = itemId;
        this.prescriptionId = prescriptionId;
        this.drugName = drugName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.quantity = quantity;
        this.instructions = instructions;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String toCsv() {
        return CsvUtil.join(itemId, prescriptionId, drugName, dosage, frequency,
                durationDays, quantity, instructions);
    }

    public static PrescriptionItem fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new PrescriptionItem(f[0], f[1], f[2], f[3], f[4],
                f[5].isBlank() ? 0 : Integer.parseInt(f[5]),
                f[6].isBlank() ? 0 : Integer.parseInt(f[6]),
                f.length > 7 ? f[7] : "");
    }

    @Override
    public String toString() {
        return String.format("%-20s %-10s %-6s %2dd  x%-3d  %s",
                drugName, dosage, frequency, durationDays, quantity, instructions);
    }
}
