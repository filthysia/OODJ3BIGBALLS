package assignment.service;

import assignment.model.AssetStatus;
import assignment.model.HospitalAsset;
import assignment.model.LabRequest;
import assignment.model.LabRequestStatus;
import assignment.model.LabTestType;
import assignment.util.IdGenerator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        if (Database.patients.stream().noneMatch(p -> p.getId().equalsIgnoreCase(key)))
            throw new IllegalArgumentException("No such patient: " + patientId);
        if (type == null)
            throw new IllegalArgumentException("Test type is required");

        String id = IdGenerator.next("LR", Database.labRequests.stream()
                .map(LabRequest::getRequestId).collect(Collectors.toList()));
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
        return Database.labRequests.stream()
                .filter(r -> r.getStatus() == status)
                .sorted(Comparator.comparing(LabRequest::getRequestedDate))
                .collect(Collectors.toList());
    }

    public List<LabRequest> forPatient(String patientId) {
        return Database.labRequests.stream()
                .filter(r -> r.getPatientId().equalsIgnoreCase(patientId))
                .sorted(Comparator.comparing(LabRequest::getRequestedDate).reversed())
                .collect(Collectors.toList());
    }

    private LabRequest require(String id) {
        String key = (id == null) ? "" : id.trim();
        return Database.labRequests.stream()
                .filter(r -> r.getRequestId().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such lab request: " + id));
    }

    private static String blank(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
