package assignment.model;

import assignment.util.CsvUtil;

/** A physical hospital resource: consultation room, inpatient ward, lab or imaging room. */
public class HospitalAsset {

    private String assetId;
    private String name;
    private AssetType type;
    private String location;
    private int capacity;
    private AssetStatus status;
    private String departmentCode;   // department it is currently allocated to (nullable)

    public HospitalAsset() {
        this.status = AssetStatus.AVAILABLE;
    }

    public HospitalAsset(String assetId, String name, AssetType type, String location,
                         int capacity, AssetStatus status, String departmentCode) {
        this.assetId = assetId;
        this.name = name;
        this.type = type;
        this.location = location;
        this.capacity = capacity;
        this.status = (status == null) ? AssetStatus.AVAILABLE : status;
        this.departmentCode = departmentCode;
    }

    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AssetType getType() { return type; }
    public void setType(AssetType type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public String toCsv() {
        return CsvUtil.join(assetId, name, type, location, capacity, status, departmentCode);
    }

    public static HospitalAsset fromCsv(String line) {
        String[] f = CsvUtil.split(line);
        return new HospitalAsset(f[0], f[1], AssetType.fromString(f[2]), f[3],
                f[4].isBlank() ? 0 : Integer.parseInt(f[4]),
                AssetStatus.fromString(f[5]),
                (f.length > 6 && !f[6].isBlank()) ? f[6] : null);
    }

    @Override
    public String toString() {
        return String.format("%-8s %-22s %-16s %-12s cap=%-3d %-11s %s",
                assetId, name, type, location, capacity, status,
                departmentCode == null ? "" : "-> " + departmentCode);
    }
}
