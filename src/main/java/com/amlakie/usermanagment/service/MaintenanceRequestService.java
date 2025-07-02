package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.MaintenanceRequestDTO;
import com.amlakie.usermanagment.entity.MaintenanceRequest;
import com.amlakie.usermanagment.exception.InvalidRequestException;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.MaintenanceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public MaintenanceRequestService(MaintenanceRequestRepository maintenanceRequestRepository) {
        this.maintenanceRequestRepository = maintenanceRequestRepository;
    }

    @Transactional
    public MaintenanceRequest createRequest(MaintenanceRequestDTO requestDTO) throws InvalidRequestException {
        validateRequest(requestDTO);
        MaintenanceRequest request = mapToEntity(requestDTO);
        request.setCreatedAt(LocalDateTime.now());
        request.setCreatedBy("driver");
        return maintenanceRequestRepository.save(request);
    }

    public List<MaintenanceRequest> getRequestsForDriver(String driverName) {
        if (driverName != null && !driverName.isEmpty()) {
            return maintenanceRequestRepository.findByReportingDriver(driverName);
        }
        return maintenanceRequestRepository.findAll();
    }

    public List<MaintenanceRequest> getRequestsForDistributor() {
        return maintenanceRequestRepository.findByStatus(MaintenanceRequest.RequestStatus.PENDING);
    }

    public List<MaintenanceRequest> getRequestsForMaintenance() {
        return maintenanceRequestRepository.findByStatus(MaintenanceRequest.RequestStatus.CHECKED);
    }

    public List<MaintenanceRequest> getRequestsForInspector() {
        return maintenanceRequestRepository.findByStatusIn(
                List.of(
                        MaintenanceRequest.RequestStatus.APPROVED,
                        MaintenanceRequest.RequestStatus.COMPLETED
                )
        );
    }

    public List<MaintenanceRequest> getRequestsForInspectorToReturn() {
        return maintenanceRequestRepository.findByStatus(MaintenanceRequest.RequestStatus.COMPLETED);
    }

    public List<MaintenanceRequest> getInspectionRequest() {
        return maintenanceRequestRepository.findByStatus(MaintenanceRequest.RequestStatus.INSPECTION);
    }
    public Optional<MaintenanceRequest> getLatestMaintenanceRequestByPlateNumber(String plateNumber) {
        return maintenanceRequestRepository.findByPlateNumber(plateNumber);
    }

    public MaintenanceRequest getRequestById(Long id) throws ResourceNotFoundException {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found with id: " + id));
    }

    @Transactional
    public MaintenanceRequest updateRequestStatus(Long id, MaintenanceRequest.RequestStatus status)
            throws ResourceNotFoundException, InvalidRequestException {
        MaintenanceRequest request = getRequestById(id);

        if (request.getStatus() == MaintenanceRequest.RequestStatus.COMPLETED) {
            throw new InvalidRequestException("Cannot change status of a completed request");
        }

        request.setStatus(status);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy("admin");
        return maintenanceRequestRepository.save(request);
    }

    @Transactional
    public MaintenanceRequest updateRequest(Long id, MaintenanceRequestDTO requestDTO)
            throws ResourceNotFoundException, InvalidRequestException {
        MaintenanceRequest request = getRequestById(id);

        validateRequest(requestDTO);
        updateEntityFromDTO(request, requestDTO);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy("admin");

        return maintenanceRequestRepository.save(request);
    }

    @Transactional
    public MaintenanceRequest submitAcceptance(Long id, MaintenanceRequestDTO acceptanceData)
            throws ResourceNotFoundException, InvalidRequestException {
        MaintenanceRequest request = getRequestById(id);

        if (request.getStatus() != MaintenanceRequest.RequestStatus.APPROVED) {
            throw new InvalidRequestException("Cannot submit acceptance for request that is not in APPROVED status");
        }

        request.setAttachments(acceptanceData.getAttachments());
        request.setPhysicalContent(acceptanceData.getPhysicalContent());
        request.setNotes(acceptanceData.getNotes());
        request.setRequestingPersonnel(acceptanceData.getRequestingPersonnel());
        request.setAuthorizingPersonnel(acceptanceData.getAuthorizingPersonnel());
        request.setFuelAmount(acceptanceData.getFuelAmount());

        if (acceptanceData.getSignatures() != null) {
            List<MaintenanceRequest.Signature> signatures = acceptanceData.getSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            request.setSignatures(signatures);
        }

        request.setStatus(MaintenanceRequest.RequestStatus.INSPECTION);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy("inspector");

        return maintenanceRequestRepository.save(request);
    }

    @Transactional
    public MaintenanceRequest completeReturnProcess(Long id, MaintenanceRequestDTO returnData)
            throws ResourceNotFoundException, InvalidRequestException {
        MaintenanceRequest request = getRequestById(id);

        if (request.getStatus() != MaintenanceRequest.RequestStatus.COMPLETED) {
            throw new InvalidRequestException(
                    "Cannot complete return for request that is not in INSPECTION or COMPLETED status"
            );
        }

        // Update return-specific fields
        if (returnData.getReturnKilometerReading() != null) {
            request.setReturnKilometerReading(returnData.getReturnKilometerReading());
        }
        if (returnData.getReturnNotes() != null) {
            request.setReturnNotes(returnData.getReturnNotes());
        }
        if (returnData.getReturnFuelAmount() != null) {
            request.setReturnFuelAmount(returnData.getReturnFuelAmount());
        }

        // Update return signatures
        if (returnData.getReturnSignatures() != null) {
            List<MaintenanceRequest.Signature> returnSignatures = returnData.getReturnSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            request.setReturnSignatures(returnSignatures);
        }

        request.setStatus(MaintenanceRequest.RequestStatus.FINISHED);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy("inspector");

        return maintenanceRequestRepository.save(request);
    }

    // MaintenanceRequestService.java - Add this method
    @Transactional
    public MaintenanceRequest completeRequest(Long id) throws ResourceNotFoundException, InvalidRequestException {
        MaintenanceRequest request = getRequestById(id);

        if (request.getStatus() != MaintenanceRequest.RequestStatus.COMPLETED) {
            throw new InvalidRequestException("Cannot complete request that is not in COMPLETED status");
        }

        request.setStatus(MaintenanceRequest.RequestStatus.FINISHED);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy("inspector");

        return maintenanceRequestRepository.save(request);
    }

    @Transactional
    public MaintenanceRequest submitReturn(Long id, MaintenanceRequestDTO returnData)
            throws ResourceNotFoundException, InvalidRequestException {
        MaintenanceRequest request = getRequestById(id);

        if (request.getStatus() != MaintenanceRequest.RequestStatus.INSPECTION) {
            throw new InvalidRequestException("Cannot submit return for request that is not in INSPECTION status");
        }

        // Update existing fields from acceptance
        if (returnData.getAttachments() != null) {
            request.setAttachments(returnData.getAttachments());
        }
        if (returnData.getPhysicalContent() != null) {
            request.setPhysicalContent(returnData.getPhysicalContent());
        }
        if (returnData.getNotes() != null) {
            request.setNotes(returnData.getNotes());
        }
        if (returnData.getSignatures() != null) {
            List<MaintenanceRequest.Signature> signatures = returnData.getSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            request.setSignatures(signatures);
        }

        // Set return-specific fields
        request.setReturnKilometerReading(returnData.getReturnKilometerReading());
        request.setReturnNotes(returnData.getReturnNotes());
        request.setReturnFuelAmount(returnData.getReturnFuelAmount());

        if (returnData.getReturnSignatures() != null) {
            List<MaintenanceRequest.Signature> returnSignatures = returnData.getReturnSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            request.setReturnSignatures(returnSignatures);
        }

        request.setStatus(MaintenanceRequest.RequestStatus.COMPLETED);
        request.setUpdatedAt(LocalDateTime.now());
        request.setUpdatedBy("inspector");

        return maintenanceRequestRepository.save(request);
    }

    @Transactional
    public MaintenanceRequest uploadFiles(Long id, MultipartFile[] files, boolean isReturnFiles)
            throws ResourceNotFoundException, InvalidRequestException, IOException {
        MaintenanceRequest request = getRequestById(id);

        if (files == null || files.length == 0) {
            throw new InvalidRequestException("No files provided");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                throw new InvalidRequestException("Invalid file type. Only images and PDFs are allowed");
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                throw new InvalidRequestException("File size exceeds 5MB limit");
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            fileUrls.add(filename);
        }

        if (isReturnFiles) {
            if (request.getReturnFiles() == null) {
                request.setReturnFiles(fileUrls);
            } else {
                request.getReturnFiles().addAll(fileUrls);
            }
        } else {
            if (request.getCarImages() == null) {
                request.setCarImages(fileUrls);
            } else {
                request.getCarImages().addAll(fileUrls);
            }
        }

        request.setUpdatedAt(LocalDateTime.now());
        return maintenanceRequestRepository.save(request);
    }

    public byte[] getFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found");
        }
        return Files.readAllBytes(filePath);
    }

    public String getFileContentType(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        String contentType = Files.probeContentType(filePath);
        return contentType != null ? contentType : "application/octet-stream";
    }

    private void validateRequest(MaintenanceRequestDTO dto) throws InvalidRequestException {
        if (isBlank(dto.getPlateNumber())) {
            throw new InvalidRequestException("Plate number is required");
        }
        if (isBlank(dto.getVehicleType())) {
            throw new InvalidRequestException("Vehicle type is required");
        }
        if (isBlank(dto.getReportingDriver())) {
            throw new InvalidRequestException("Reporting driver is required");
        }
        if (isBlank(dto.getCategoryWorkProcess())) {
            throw new InvalidRequestException("Category/work process is required");
        }
        if (dto.getKilometerReading() == null) {
            throw new InvalidRequestException("Kilometer reading is required");
        }
        if (isBlank(dto.getDefectDetails())) {
            throw new InvalidRequestException("Defect details are required");
        }
        if (dto.getDefectDetails().length() > 500) {
            throw new InvalidRequestException("Defect details cannot exceed 500 characters");
        }
        if (dto.getMechanicDiagnosis() != null && dto.getMechanicDiagnosis().length() > 500) {
            throw new InvalidRequestException("Mechanic diagnosis cannot exceed 500 characters");
        }
    }

    private MaintenanceRequest mapToEntity(MaintenanceRequestDTO dto) {
        MaintenanceRequest entity = new MaintenanceRequest();
        entity.setPlateNumber(dto.getPlateNumber());
        entity.setVehicleType(dto.getVehicleType());
        entity.setReportingDriver(dto.getReportingDriver());
        entity.setCategoryWorkProcess(dto.getCategoryWorkProcess());
        entity.setKilometerReading(dto.getKilometerReading());
        entity.setDefectDetails(dto.getDefectDetails());
        entity.setMechanicDiagnosis(dto.getMechanicDiagnosis());
        entity.setRequestingPersonnel(dto.getRequestingPersonnel());
        entity.setAuthorizingPersonnel(dto.getAuthorizingPersonnel());
        entity.setFuelAmount(dto.getFuelAmount());
        entity.setAttachments(dto.getAttachments());
        entity.setPhysicalContent(dto.getPhysicalContent());
        entity.setNotes(dto.getNotes());
        entity.setReturnKilometerReading(dto.getReturnKilometerReading());
        entity.setReturnNotes(dto.getReturnNotes());
        entity.setReturnFuelAmount(dto.getReturnFuelAmount());
        entity.setReturnFiles(dto.getReturnFiles());

        if (dto.getStatus() != null) {
            entity.setStatus(MaintenanceRequest.RequestStatus.valueOf(dto.getStatus()));
        }

        if (dto.getSignatures() != null) {
            List<MaintenanceRequest.Signature> signatures = dto.getSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            entity.setSignatures(signatures);
        }

        if (dto.getReturnSignatures() != null) {
            List<MaintenanceRequest.Signature> returnSignatures = dto.getReturnSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            entity.setReturnSignatures(returnSignatures);
        }

        return entity;
    }

    private void updateEntityFromDTO(MaintenanceRequest entity, MaintenanceRequestDTO dto) {
        if (dto.getPlateNumber() != null) {
            entity.setPlateNumber(dto.getPlateNumber());
        }
        if (dto.getVehicleType() != null) {
            entity.setVehicleType(dto.getVehicleType());
        }
        if (dto.getReportingDriver() != null) {
            entity.setReportingDriver(dto.getReportingDriver());
        }
        if (dto.getCategoryWorkProcess() != null) {
            entity.setCategoryWorkProcess(dto.getCategoryWorkProcess());
        }
        if (dto.getKilometerReading() != null) {
            entity.setKilometerReading(dto.getKilometerReading());
        }
        if (dto.getDefectDetails() != null) {
            entity.setDefectDetails(dto.getDefectDetails());
        }
        if (dto.getMechanicDiagnosis() != null) {
            entity.setMechanicDiagnosis(dto.getMechanicDiagnosis());
        }
        if (dto.getRequestingPersonnel() != null) {
            entity.setRequestingPersonnel(dto.getRequestingPersonnel());
        }
        if (dto.getAuthorizingPersonnel() != null) {
            entity.setAuthorizingPersonnel(dto.getAuthorizingPersonnel());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(MaintenanceRequest.RequestStatus.valueOf(dto.getStatus()));
        }
        if (dto.getFuelAmount() != null) {
            entity.setFuelAmount(dto.getFuelAmount());
        }
        if (dto.getAttachments() != null) {
            entity.setAttachments(dto.getAttachments());
        }
        if (dto.getPhysicalContent() != null) {
            entity.setPhysicalContent(dto.getPhysicalContent());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
        if (dto.getReturnKilometerReading() != null) {
            entity.setReturnKilometerReading(dto.getReturnKilometerReading());
        }
        if (dto.getReturnNotes() != null) {
            entity.setReturnNotes(dto.getReturnNotes());
        }
        if (dto.getReturnFuelAmount() != null) {
            entity.setReturnFuelAmount(dto.getReturnFuelAmount());
        }
        if (dto.getReturnFiles() != null) {
            entity.setReturnFiles(dto.getReturnFiles());
        }
        if (dto.getSignatures() != null) {
            List<MaintenanceRequest.Signature> signatures = dto.getSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            entity.setSignatures(signatures);
        }
        if (dto.getReturnSignatures() != null) {
            List<MaintenanceRequest.Signature> returnSignatures = dto.getReturnSignatures().stream()
                    .map(sig -> {
                        MaintenanceRequest.Signature signature = new MaintenanceRequest.Signature();
                        signature.setRole(sig.getRole());
                        signature.setName(sig.getName());
                        signature.setSignature(sig.getSignature());
                        signature.setDate(sig.getDate());
                        return signature;
                    })
                    .collect(Collectors.toList());
            entity.setReturnSignatures(returnSignatures);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}