package assignment.service;

import assignment.model.AssetStatus;
import assignment.model.AssetType;
import assignment.model.HospitalAsset;
import assignment.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/** Use-case: Admin Staff manages and allocates physical hospital assets. */
public class AssetService {

    private final DepartmentService departmentService = new DepartmentService();

    public List<HospitalAsset> getAll() {
        return Database.assets;
    }

    public HospitalAsset findById(String id) {
        String key = (id == null) ? "" : id.trim();
        for (HospitalAsset a : Database.assets) {
            if (a.getAssetId().equalsIgnoreCase(key)) return a;
        }
        return null;
    }

    public List<HospitalAsset> byType(AssetType type) {
        List<HospitalAsset> result = new ArrayList<>();
        for (HospitalAsset a : Database.assets) {
            if (a.getType() == type) result.add(a);
        }
        return result;
    }

    public List<HospitalAsset> availableByType(AssetType type) {
        List<HospitalAsset> result = new ArrayList<>();
        for (HospitalAsset a : Database.assets) {
            if (a.getType() == type && a.getStatus() == AssetStatus.AVAILABLE) result.add(a);
        }
        return result;
    }

    public HospitalAsset create(String name, AssetType type, String location, int capacity) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Asset name is required");
        if (type == null)
            throw new IllegalArgumentException("Asset type is required");
        if (capacity < 0)
            throw new IllegalArgumentException("Capacity cannot be negative");

        List<String> existingIds = new ArrayList<>();
        for (HospitalAsset a : Database.assets) existingIds.add(a.getAssetId());
        String id = IdGenerator.next("AST", existingIds);

        HospitalAsset a = new HospitalAsset(id, name.trim(), type,
                location == null ? "" : location.trim(), capacity, AssetStatus.AVAILABLE, null);
        Database.assets.add(a);
        Database.saveAssets();
        return a;
    }

    public HospitalAsset update(String id, String name, String location, Integer capacity, AssetStatus status) {
        HospitalAsset a = require(id);
        if (name != null && !name.isBlank()) a.setName(name.trim());
        if (location != null) a.setLocation(location.trim());
        if (capacity != null) {
            if (capacity < 0) throw new IllegalArgumentException("Capacity cannot be negative");
            a.setCapacity(capacity);
        }
        if (status != null) {
            a.setStatus(status);
            if (status != AssetStatus.ALLOCATED) a.setDepartmentCode(null);
        }
        Database.saveAssets();
        return a;
    }

    public void delete(String id) {
        HospitalAsset a = require(id);
        Database.assets.remove(a);
        Database.saveAssets();
    }

    /** Allocate an asset to a department. */
    public void allocate(String id, String departmentCode) {
        HospitalAsset a = require(id);
        if (departmentService.findByCode(departmentCode) == null)
            throw new IllegalArgumentException("No such department: " + departmentCode);
        if (a.getStatus() == AssetStatus.MAINTENANCE || a.getStatus() == AssetStatus.RETIRED)
            throw new IllegalStateException("Asset is " + a.getStatus() + " and cannot be allocated");
        a.setDepartmentCode(departmentCode.trim().toUpperCase());
        a.setStatus(AssetStatus.ALLOCATED);
        Database.saveAssets();
    }

    public void release(String id) {
        HospitalAsset a = require(id);
        a.setDepartmentCode(null);
        a.setStatus(AssetStatus.AVAILABLE);
        Database.saveAssets();
    }

    private HospitalAsset require(String id) {
        HospitalAsset a = findById(id);
        if (a == null) throw new IllegalArgumentException("No such asset: " + id);
        return a;
    }
}
