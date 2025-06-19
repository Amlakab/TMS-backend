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
            throw new InvalidRequestException("Cannot submit acceptance for request that is not in CHECKED status");
        }

        request.setAttachments(acceptanceData.getAttachments());
        request.setPhysicalContent(acceptanceData.getPhysicalContent());
        request.setNotes(acceptanceData.getNotes());
        request.setRequestingPersonnel(acceptanceData.getRequestingPersonnel());
        request.setAuthorizingPersonnel(acceptanceData.getAuthorizingPersonnel());

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
        request.setUpdatedBy("driver");

        return maintenanceRequestRepository.save(request);
    }

    @Transactional
    public MaintenanceRequest uploadFiles(Long id, MultipartFile[] files)
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
            // Validate file type and size
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))){
                throw new InvalidRequestException("Invalid file type. Only images and PDFs are allowed");
            }

            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                throw new InvalidRequestException("File size exceeds 5MB limit");
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            fileUrls.add(filename); // Store only filename
        }

        if (request.getCarImages() == null) {
            request.setCarImages(fileUrls);
        } else {
            request.getCarImages().addAll(fileUrls);
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
        entity.setAttachments(dto.getAttachments());
        entity.setPhysicalContent(dto.getPhysicalContent());
        entity.setNotes(dto.getNotes());

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
        if (dto.getAttachments() != null) {
            entity.setAttachments(dto.getAttachments());
        }
        if (dto.getPhysicalContent() != null) {
            entity.setPhysicalContent(dto.getPhysicalContent());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
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
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}