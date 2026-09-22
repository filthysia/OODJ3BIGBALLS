package assignment.service;

import assignment.model.AssetStatus;
import assignment.model.HospitalAsset;
import assignment.model.LabRequest;
import assignment.model.LabRequestStatus;
import assignment.model.LabTestType;
import assignment.model.Patient;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Use-case: a doctor issues requests to Admin Staff for lab tests, X-rays or
 * specialised imaging; Admin Staff schedules them onto an asset and records results.
 */
public class LabRequestService {

    private final AssetService assetService = new AssetService();

    // ----- doctor -----

    public LabRequest raise(String patientId, String doctorId, String appointmentId,
                            LabTestType type, String clinicalReason) {
        String key = (patientId == null) ? "" : patientId.trim();
        boolean patientExists = false;
        for (Patient p : Database.patients) {
            if (p.getId().equalsIgnoreCase(key)) {
                patientExists = true;
                break;
            }
        }
        if (!patientExists) throw new IllegalArgumentException("No such patient: " + patientId);
        if (type == null)
            throw new IllegalArgumentException("Test type is required");

        List<String> existingIds = new ArrayList<>();
        for (LabRequest r : Database.labRequests) existingIds.add(r.getRequestId());
        String id = IdGenerator.next("LR", existingIds);

        LabRequest r = new LabRequest(id, key, doctorId, blank(appointmentId), type, LocalDate.now(),
                LabRequestStatus.REQUESTED, null, nz(clinicalReason), "");
        Database.labRequests.add(r);
        Database.saveLabRequests();
        return r;
    }

    // ----- admin staff -----

    public void schedule(String requestId, String assetId) {
        LabRequest r = require(requestId);
        if (r.getStatus() != LabRequestStatus.REQUESTED)
            throw new IllegalStateException("Request is already " + r.getStatus());
        HospitalAsset asset = assetService.findById(assetId);
        if (asset == null) throw new IllegalArgumentException("No such asset: " + assetId);
        if (asset.getType() != r.getType().getRequiredAsset())
            throw new IllegalArgumentException(r.getType() + " needs a "
                    + r.getType().getRequiredAsset() + " asset, not " + asset.getType());
        if (asset.getStatus() == AssetStatus.MAINTENANCE || asset.getStatus() == AssetStatus.RETIRED)
            throw new IllegalStateException("Asset is " + asset.getStatus());
        r.setAssignedAssetId(asset.getAssetId());
        r.setStatus(LabRequestStatus.SCHEDULED);
        Database.saveLabRequests();
    }

    public void complete(String requestId, String resultNotes) {
        LabRequest r = require(requestId);
        if (r.getStatus() != LabRequestStatus.SCHEDULED)
            throw new IllegalStateException("Only scheduled requests can be completed");
        r.setResultNotes(nz(resultNotes));
        r.setStatus(LabRequestStatus.COMPLETED);
        Database.saveLabRequests();
    }

    public void cancel(String requestId) {
        LabRequest r = require(requestId);
        if (r.getStatus() == LabRequestStatus.COMPLETED)
            throw new IllegalStateException("Completed requests cannot be cancelled");
        r.setStatus(LabRequestStatus.CANCELLED);
        Database.saveLabRequests();
    }

    // ----- queries -----

    public List<LabRequest> pending() {
        return byStatus(LabRequestStatus.REQUESTED);
    }

    public List<LabRequest> byStatus(LabRequestStatus status) {
        List<LabRequest> result = new ArrayList<>();
        for (LabRequest r : Database.labRequests) {
            if (r.getStatus() == status) result.add(r);
        }
        sortByRequestedDate(result, false);
        return result;
    }

    public List<LabRequest> forPatient(String patientId) {
        List<LabRequest> result = new ArrayList<>();
        for (LabRequest r : Database.labRequests) {
            if (r.getPatientId().equalsIgnoreCase(patientId)) result.add(r);
        }
        sortByRequestedDate(result, true);
        return result;
    }

    private void sortByRequestedDate(List<LabRequest> list, boolean descending) {
        for (int i = 1; i < list.size(); i++) {
            LabRequest current = list.get(i);
            int j = i - 1;
            while (j >= 0 && shouldSwap(list.get(j), current, descending)) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, current);
        }
    }

    private boolean shouldSwap(LabRequest earlier, LabRequest current, boolean descending) {
        return descending
                ? earlier.getRequestedDate().isBefore(current.getRequestedDate())
                : earlier.getRequestedDate().isAfter(current.getRequestedDate());
    }

    private LabRequest require(String id) {
        String key = (id == null) ? "" : id.trim();
        for (LabRequest r : Database.labRequests) {
            if (r.getRequestId().equalsIgnoreCase(key)) return r;
        }
        throw new IllegalArgumentException("No such lab request: " + id);
    }

    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
