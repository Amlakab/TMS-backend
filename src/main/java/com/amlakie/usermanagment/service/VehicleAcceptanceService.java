package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.SignatureRequest;
import com.amlakie.usermanagment.dto.SignatureResponse;
import com.amlakie.usermanagment.dto.VehicleAcceptanceRequest;
import com.amlakie.usermanagment.dto.VehicleAcceptanceResponse;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.entity.Signature;
import com.amlakie.usermanagment.entity.VehicleAcceptance;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.repository.VehicleAcceptanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VehicleAcceptanceService {

    private final FileStorageService fileStorageService;
    private final VehicleAcceptanceRepository vehicleRepo;
    private final AssignmentHistoryRepository assignmentRepo;

    @Autowired
    public VehicleAcceptanceService(FileStorageService fileStorageService,
                                    VehicleAcceptanceRepository vehicleRepo,
                                    AssignmentHistoryRepository assignmentRepo) {
        this.fileStorageService = fileStorageService;
        this.vehicleRepo = vehicleRepo;
        this.assignmentRepo = assignmentRepo;
    }

    public VehicleAcceptanceResponse create(VehicleAcceptanceRequest request, MultipartFile[] images) throws IOException {
        return saveVehicleAcceptance(null, request, images);
    }

    public VehicleAcceptanceResponse update(Long id, VehicleAcceptanceRequest request, MultipartFile[] images) throws IOException {
        if (!vehicleRepo.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle acceptance not found with id: " + id);
        }
        return saveVehicleAcceptance(id, request, images);
    }

    public Page<VehicleAcceptanceResponse> getAllVehicleAcceptances(Pageable pageable) {
        Page<VehicleAcceptance> acceptances = vehicleRepo.findAllByOrderByCreatedAtDesc(pageable);
        return acceptances.map(acceptance ->
                toResponse(acceptance, "Vehicle acceptance retrieved", 200));
    }

    public List<VehicleAcceptanceResponse> getAllVehicleAcceptances() {
        List<VehicleAcceptance> acceptances = vehicleRepo.findAllByOrderByCreatedAtDesc();
        return acceptances.stream()
                .map(acceptance -> toResponse(acceptance, "Vehicle acceptance retrieved", 200))
                .collect(Collectors.toList());
    }

    public VehicleAcceptanceResponse getById(Long id) {
        VehicleAcceptance acceptance = vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle acceptance not found with id: " + id));
        return toResponse(acceptance, "Vehicle acceptance retrieved successfully", 200);
    }

    public VehicleAcceptanceResponse getLatestByPlateNumber(String plateNumber) {
        VehicleAcceptance acceptance = vehicleRepo.findTopByPlateNumberOrderByCreatedAtDesc(plateNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle acceptance not found for plate number: " + plateNumber));
        return toResponse(acceptance, "Latest vehicle acceptance retrieved successfully", 200);
    }

    public List<VehicleAcceptanceResponse> getAllByPlateNumber(String plateNumber) {
        List<VehicleAcceptance> acceptances = vehicleRepo.findByPlateNumberOrderByCreatedAtDesc(plateNumber);

        if (acceptances.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No vehicle acceptances found for plate number: " + plateNumber);
        }

        return acceptances.stream()
                .map(acceptance -> toResponse(acceptance, "Vehicle acceptance retrieved", 200))
                .collect(Collectors.toList());
    }

    public VehicleAcceptanceResponse getByAssignmentHistoryId(Long assignmentHistoryId) {
        VehicleAcceptance acceptance = vehicleRepo.findTopByAssignmentHistoryIdOrderByCreatedAtDesc(assignmentHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle acceptance not found for assignment: " + assignmentHistoryId));
        return toResponse(acceptance, "Latest vehicle acceptance retrieved successfully", 200);
    }

    public List<VehicleAcceptanceResponse> getAllByAssignmentHistoryId(Long assignmentHistoryId) {
        List<VehicleAcceptance> acceptances = vehicleRepo.findByAssignmentHistoryIdOrderByCreatedAtDesc(assignmentHistoryId);

        if (acceptances.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No vehicle acceptances found for assignment: " + assignmentHistoryId);
        }

        return acceptances.stream()
                .map(acceptance -> toResponse(acceptance, "Vehicle acceptance retrieved", 200))
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws IOException {
        VehicleAcceptance acceptance = vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle acceptance not found with id: " + id));

        // Delete associated images from storage
        if (acceptance.getCarImages() != null) {
            for (String imageUrl : acceptance.getCarImages()) {
                fileStorageService.deleteFile(imageUrl);
            }
        }

        vehicleRepo.delete(acceptance);
    }

    private VehicleAcceptanceResponse saveVehicleAcceptance(
            Long id,
            VehicleAcceptanceRequest request,
            MultipartFile[] images) throws IOException {

        VehicleAcceptance acceptance = id != null ?
                vehicleRepo.findById(id).orElse(new VehicleAcceptance()) :
                new VehicleAcceptance();

        // Handle images - now working with full URLs
        List<String> imageUrls = new ArrayList<>();

        // For updates, keep existing images unless removed
        if (id != null && acceptance.getCarImages() != null) {
            if (request.getExistingImageUrls() != null) {
                // Filter to only keep URLs that are in the request
                imageUrls.addAll(acceptance.getCarImages().stream()
                        .filter(url -> request.getExistingImageUrls().contains(url))
                        .collect(Collectors.toList()));

                // Delete images that were removed
                List<String> imagesToRemove = new ArrayList<>(acceptance.getCarImages());
                imagesToRemove.removeAll(request.getExistingImageUrls());
                for (String removedImage : imagesToRemove) {
                    fileStorageService.deleteFile(removedImage);
                }
            }
        }

        // Upload new images
        if (images != null && images.length > 0) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String imageUrl = fileStorageService.storeFile(file);
                    imageUrls.add(imageUrl);
                }
            }
        }

        // Update vehicle acceptance data
        acceptance.setPlateNumber(request.getPlateNumber());
        acceptance.setCarType(request.getCarType());
        acceptance.setKm(request.getKm());
        acceptance.setInspectionItems(request.getInspectionItems());
        acceptance.setAttachments(request.getAttachments());
        acceptance.setPhysicalContent(request.getPhysicalContent());
        acceptance.setNotes(request.getNotes());
        acceptance.setCarImages(imageUrls);

        // Handle signatures
        if (request.getSignatures() != null) {
            // For updates, clear existing signatures first
            if (id != null && acceptance.getSignatures() != null) {
                acceptance.getSignatures().clear();
            }

            List<Signature> signatures = request.getSignatures().stream()
                    .map(this::toSignatureEntity)
                    .peek(sig -> sig.setVehicleAcceptance(acceptance))
                    .collect(Collectors.toList());
            acceptance.setSignatures(signatures);
        }

        // Set assignment history if provided
        if (request.getAssignmentHistoryId() != null) {
            AssignmentHistory history = assignmentRepo.findById(request.getAssignmentHistoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignment history not found with id: " + request.getAssignmentHistoryId()));
            acceptance.setAssignmentHistory(history);
        }

        // Set timestamps
        if (acceptance.getCreatedAt() == null) {
            acceptance.setCreatedAt(LocalDateTime.now());
        }
        acceptance.setUpdatedAt(LocalDateTime.now());

        // Save and return response
        VehicleAcceptance savedAcceptance = vehicleRepo.save(acceptance);
        return toResponse(savedAcceptance, id != null ?
                "Vehicle acceptance updated successfully" :
                "Vehicle acceptance created successfully", 200);
    }

    private Signature toSignatureEntity(SignatureRequest req) {
        Signature sig = new Signature();
        sig.setName(req.getName());
        sig.setRole(req.getRole());
        sig.setSignature(req.getSignature());
        sig.setDate(req.getDate());
        return sig;
    }

    private VehicleAcceptanceResponse toResponse(VehicleAcceptance acceptance, String message, int statusCode) {
        VehicleAcceptanceResponse res = new VehicleAcceptanceResponse();
        res.setId(acceptance.getId());
        res.setPlateNumber(acceptance.getPlateNumber());
        res.setCarType(acceptance.getCarType());
        res.setKm(acceptance.getKm());
        res.setInspectionItems(acceptance.getInspectionItems());
        res.setAttachments(acceptance.getAttachments());
        res.setCarImages(acceptance.getCarImages());
        res.setPhysicalContent(acceptance.getPhysicalContent());
        res.setNotes(acceptance.getNotes());

        if (acceptance.getSignatures() != null) {
            res.setSignatures(acceptance.getSignatures().stream()
                    .map(sig -> new SignatureResponse(
                            sig.getRole(),
                            sig.getName(),
                            sig.getSignature(),
                            sig.getDate()))
                    .collect(Collectors.toList()));
        }

        res.setCreatedAt(acceptance.getCreatedAt());
        res.setUpdatedAt(acceptance.getUpdatedAt());

        if (acceptance.getAssignmentHistory() != null) {
            res.setAssignmentHistoryId(acceptance.getAssignmentHistory().getId());
        }

        res.setMessage(message);
        res.setStatusCode(statusCode);
        return res;
    }
}