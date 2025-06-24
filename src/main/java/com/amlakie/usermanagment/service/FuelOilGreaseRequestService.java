package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.fogrequest.FuelOilGreaseRequestDTO;
import com.amlakie.usermanagment.dto.fogrequest.RequestItemDTO;
import com.amlakie.usermanagment.dto.fogrequest.FillDetailsDTO;
import com.amlakie.usermanagment.entity.*;
import com.amlakie.usermanagment.entity.fogrequest.*;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.FuelOilGreaseRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelOilGreaseRequestService {
    private final FuelOilGreaseRequestRepository requestRepository;

    @Transactional
    public FuelOilGreaseRequestDTO createRequest(FuelOilGreaseRequestDTO requestDTO) {
        FuelOilGreaseRequest request = convertToEntity(requestDTO);
        request.setRequestDate(LocalDate.now());
        request.setStatus(RequestStatus.DRAFT);
        request.setHeadMechanicApproval(ApprovalStatus.PENDING);
        request.setNezekStatus(NezekStatus.PENDING);
        request.setIsFulfilled(false);

        FuelOilGreaseRequest savedRequest = requestRepository.save(request);
        return convertToDTO(savedRequest);
    }

    @Transactional
    public FuelOilGreaseRequestDTO submitRequest(Long id) {
        FuelOilGreaseRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        request.setStatus(RequestStatus.PENDING);
        return convertToDTO(requestRepository.save(request));
    }

    @Transactional
    public FuelOilGreaseRequestDTO headMechanicReview(Long id, String headMechanicName, boolean isApproved) {
        FuelOilGreaseRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not in PENDING status");
        }

        request.setHeadMechanicName(headMechanicName);
        request.setHeadMechanicApproval(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        request.setStatus(isApproved ? RequestStatus.CHECKED : RequestStatus.REJECTED);

        return convertToDTO(requestRepository.save(request));
    }

    @Transactional
    public FuelOilGreaseRequestDTO nezekReview(Long id, String nezekOfficialName, boolean isApproved) {
        FuelOilGreaseRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != RequestStatus.CHECKED || request.getHeadMechanicApproval() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Request is not ready for NEZEK review");
        }

        request.setNezekOfficialName(nezekOfficialName);
        request.setNezekStatus(isApproved ? NezekStatus.APPROVED : NezekStatus.REJECTED);
        request.setStatus(isApproved ? RequestStatus.APPROVED : RequestStatus.REJECTED);

        return convertToDTO(requestRepository.save(request));
    }

    @Transactional
    public FuelOilGreaseRequestDTO fulfillRequest(Long id, List<RequestItemDTO> filledItems) {
        FuelOilGreaseRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != RequestStatus.APPROVED ||
                request.getHeadMechanicApproval() != ApprovalStatus.APPROVED ||
                request.getNezekStatus() != NezekStatus.APPROVED) {
            throw new IllegalStateException("Request is not approved for fulfillment");
        }

        // Update filled details manually
        filledItems.forEach(filledItem -> {
            request.getItems().stream()
                    .filter(item -> item.getId().equals(filledItem.getId()))
                    .findFirst()
                    .ifPresent(item -> {
                        if (filledItem.getFilled() != null) {
                            FillDetails filledDetails = new FillDetails();
                            filledDetails.setMeasurement(filledItem.getFilled().getMeasurement());
                            filledDetails.setAmount(filledItem.getFilled().getAmount());
                            filledDetails.setPrice(filledItem.getFilled().getPrice());
                            item.setFilled(filledDetails);
                        }
                    });
        });

        request.setIsFulfilled(true);
        request.setStatus(RequestStatus.FULFILLED);

        return convertToDTO(requestRepository.save(request));
    }

    public List<FuelOilGreaseRequestDTO> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getCheckedRequests() {
        return requestRepository.findByHeadMechanicApprovalAndStatus(ApprovalStatus.APPROVED, RequestStatus.CHECKED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getApprovedRequests() {
        return requestRepository.findByNezekStatusAndStatusAndIsFulfilled(NezekStatus.APPROVED, RequestStatus.APPROVED, false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getRequestsByMechanic(String mechanicName) {
        return requestRepository.findByMechanicName(mechanicName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public FuelOilGreaseRequestDTO getRequestById(Long id) {
        return requestRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
    }

    private FuelOilGreaseRequestDTO convertToDTO(FuelOilGreaseRequest request) {
        FuelOilGreaseRequestDTO dto = new FuelOilGreaseRequestDTO();
        dto.setId(request.getId());
        dto.setRequestDate(request.getRequestDate());
        dto.setCarType(request.getCarType());
        dto.setPlateNumber(request.getPlateNumber());
        dto.setKmReading(request.getKmReading());
        dto.setShortExplanation(request.getShortExplanation());

        if (request.getItems() != null) {
            dto.setItems(request.getItems().stream()
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList()));
        }

        dto.setMechanicName(request.getMechanicName());
        dto.setHeadMechanicName(request.getHeadMechanicName());
        dto.setHeadMechanicApproval(request.getHeadMechanicApproval());
        dto.setNezekOfficialName(request.getNezekOfficialName());
        dto.setNezekStatus(request.getNezekStatus());
        dto.setIsFulfilled(request.getIsFulfilled());
        dto.setStatus(request.getStatus());

        return dto;
    }

    private RequestItemDTO convertItemToDTO(RequestItem item) {
        RequestItemDTO dto = new RequestItemDTO();
        dto.setId(item.getId());
        dto.setType(item.getType());

        if (item.getRequested() != null) {
            FillDetailsDTO requested = new FillDetailsDTO();
            requested.setMeasurement(item.getRequested().getMeasurement());
            requested.setAmount(item.getRequested().getAmount());
            requested.setPrice(item.getRequested().getPrice());
            dto.setRequested(requested);
        }

        if (item.getFilled() != null) {
            FillDetailsDTO filled = new FillDetailsDTO();
            filled.setMeasurement(item.getFilled().getMeasurement());
            filled.setAmount(item.getFilled().getAmount());
            filled.setPrice(item.getFilled().getPrice());
            dto.setFilled(filled);
        }

        dto.setDetails(item.getDetails());
        return dto;
    }

    private FuelOilGreaseRequest convertToEntity(FuelOilGreaseRequestDTO dto) {
        FuelOilGreaseRequest entity = new FuelOilGreaseRequest();
        entity.setId(dto.getId());
        entity.setRequestDate(dto.getRequestDate());
        entity.setCarType(dto.getCarType());
        entity.setPlateNumber(dto.getPlateNumber());
        entity.setKmReading(dto.getKmReading());
        entity.setShortExplanation(dto.getShortExplanation());

        if (dto.getItems() != null) {
            entity.setItems(dto.getItems().stream()
                    .map(this::convertItemToEntity)
                    .collect(Collectors.toList()));
        }

        entity.setMechanicName(dto.getMechanicName());
        entity.setHeadMechanicName(dto.getHeadMechanicName());
        entity.setHeadMechanicApproval(dto.getHeadMechanicApproval());
        entity.setNezekOfficialName(dto.getNezekOfficialName());
        entity.setNezekStatus(dto.getNezekStatus());
        entity.setIsFulfilled(dto.getIsFulfilled());
        entity.setStatus(dto.getStatus());

        return entity;
    }

    private RequestItem convertItemToEntity(RequestItemDTO dto) {
        RequestItem item = new RequestItem();
        item.setId(dto.getId());
        item.setType(dto.getType());

        if (dto.getRequested() != null) {
            FillDetails requested = new FillDetails();
            requested.setMeasurement(dto.getRequested().getMeasurement());
            requested.setAmount(dto.getRequested().getAmount());
            requested.setPrice(dto.getRequested().getPrice());
            item.setRequested(requested);
        }

        if (dto.getFilled() != null) {
            FillDetails filled = new FillDetails();
            filled.setMeasurement(dto.getFilled().getMeasurement());
            filled.setAmount(dto.getFilled().getAmount());
            filled.setPrice(dto.getFilled().getPrice());
            item.setFilled(filled);
        }

        item.setDetails(dto.getDetails());
        return item;
    }
}