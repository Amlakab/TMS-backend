package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.fogrequest.FuelOilGreaseRequestDTO;
import com.amlakie.usermanagment.dto.fogrequest.RequestItemDTO;
import com.amlakie.usermanagment.entity.fogrequest.*;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.FuelOilGreaseRequestRepository;
import com.amlakie.usermanagment.service.util.FuelRequestConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FuelOilGreaseRequestService {
    private final FuelOilGreaseRequestRepository requestRepository;
    private static final Logger log = LoggerFactory.getLogger(FuelOilGreaseRequestService.class);

    private FuelOilGreaseRequest findRequestByIdOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
    }

    @Transactional
    public FuelOilGreaseRequestDTO createRequest(FuelOilGreaseRequestDTO requestDTO) {
        FuelOilGreaseRequest request = FuelRequestConverter.toEntity(requestDTO);

        // Set system-managed properties
        request.setMechanicName(requestDTO.getMechanicName());
        request.setRequestDate(LocalDate.now());
        request.setStatus(RequestStatus.PENDING);
        request.setHeadMechanicApproval(ApprovalStatus.PENDING);
        request.setNezekStatus(NezekStatus.PENDING);
        request.setIsFulfilled(false);

        if (request.getItems() != null) {
            request.getItems().forEach(item -> item.setRequest(request));
        }

        FuelOilGreaseRequest savedRequest = requestRepository.save(request);
        return FuelRequestConverter.toDto(savedRequest);
    }

    @Transactional
    public FuelOilGreaseRequestDTO submitRequest(Long id) {
        FuelOilGreaseRequest request = findRequestByIdOrThrow(id);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request can only be submitted if its status is DRAFT. Current status: " + request.getStatus());
        }
        request.setStatus(RequestStatus.PENDING);
        return FuelRequestConverter.toDto(requestRepository.save(request));
    }

    @Transactional
    public FuelOilGreaseRequestDTO headMechanicReview(Long id, FuelOilGreaseRequestDTO reviewDTO) {
        FuelOilGreaseRequest request = findRequestByIdOrThrow(id);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request can only be reviewed if its status is PENDING. Current status: " + request.getStatus());
        }

        // Update the core fields from the DTO
        request.setCarType(reviewDTO.getCarType());
        request.setPlateNumber(reviewDTO.getPlateNumber());
        request.setKmReading(reviewDTO.getKmReading());
        request.setShortExplanation(reviewDTO.getShortExplanation());

        // Approval logic
        boolean isApproved = reviewDTO.getHeadMechanicApproved() != null && reviewDTO.getHeadMechanicApproved();

        request.setHeadMechanicName(reviewDTO.getHeadMechanicName());
        request.setHeadMechanicApproval(isApproved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        request.setStatus(isApproved ? RequestStatus.CHECKED : RequestStatus.REJECTED);

        FuelOilGreaseRequest savedRequest = requestRepository.save(request);
        return FuelRequestConverter.toDto(savedRequest);
    }

    @Transactional
    public FuelOilGreaseRequestDTO nezekReview(Long id, FuelOilGreaseRequestDTO reviewDTO) {
        FuelOilGreaseRequest request = findRequestByIdOrThrow(id);

        if (request.getStatus() != RequestStatus.CHECKED || request.getHeadMechanicApproval() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Request is not ready for NEZEK review. Current status: " + request.getStatus() + ", Head Mechanic Approval: " + request.getHeadMechanicApproval());
        }

        // Update core fields
        request.setCarType(reviewDTO.getCarType());
        request.setPlateNumber(reviewDTO.getPlateNumber());
        request.setKmReading(reviewDTO.getKmReading());
        request.setShortExplanation(reviewDTO.getShortExplanation());

        // Approval logic
        boolean isApproved = reviewDTO.getNezekOfficialStatus() == NezekStatus.APPROVED;

        request.setNezekOfficialName(reviewDTO.getNezekOfficialName());
        request.setNezekStatus(isApproved ? NezekStatus.APPROVED : NezekStatus.REJECTED);
        request.setStatus(isApproved ? RequestStatus.APPROVED : RequestStatus.REJECTED);

        FuelOilGreaseRequest savedRequest = requestRepository.save(request);
        return FuelRequestConverter.toDto(savedRequest);
    }

    @Transactional
    public FuelOilGreaseRequestDTO fulfillRequest(Long id, FuelOilGreaseRequestDTO requestDTO) {
        FuelOilGreaseRequest request = findRequestByIdOrThrow(id);
        if (request.getStatus() != RequestStatus.APPROVED || request.getHeadMechanicApproval() != ApprovalStatus.APPROVED || request.getNezekStatus() != NezekStatus.APPROVED) {
            throw new IllegalStateException("Request is not approved for fulfillment. Current status: " + request.getStatus() + ", HM Approval: " + request.getHeadMechanicApproval() + ", Nezek Status: " + request.getNezekStatus());
        }

        Map<Long, RequestItem> existingItemMap = request.getItems().stream().collect(Collectors.toMap(RequestItem::getId, item -> item));
        List<RequestItemDTO> incomingItemDTOs = FuelRequestConverter.extractItemDTOs(requestDTO);

        incomingItemDTOs.forEach(incomingItemDto -> {
            if (incomingItemDto.getId() != null) {
                RequestItem itemToUpdate = existingItemMap.get(incomingItemDto.getId());
                if (itemToUpdate != null) {
                    if (incomingItemDto.getFilled() != null) {
                        itemToUpdate.setFilled(FuelRequestConverter.toFillDetailsEntity(incomingItemDto.getFilled()));
                    }
                } else {
                    log.warn("Incoming RequestItemDTO with ID {} not found in request {}", incomingItemDto.getId(), id);
                }
            }
        });

        request.setIsFulfilled(true);
        request.setStatus(RequestStatus.FULFILLED);
        FuelOilGreaseRequest savedRequest = requestRepository.save(request);
        return FuelRequestConverter.toDto(savedRequest);
    }

    public List<FuelOilGreaseRequestDTO> getDraftRequests() {
        return requestRepository.findByStatus(RequestStatus.DRAFT)
                .stream()
                .map(FuelRequestConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING).stream()
                .map(FuelRequestConverter::toDto).collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getCheckedRequests() {
        return requestRepository.findByHeadMechanicApprovalAndStatus(ApprovalStatus.APPROVED, RequestStatus.CHECKED).stream()
                .map(FuelRequestConverter::toDto).collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getApprovedRequests() {
        return requestRepository.findByNezekStatusAndStatusAndIsFulfilled(NezekStatus.APPROVED, RequestStatus.APPROVED, false).stream()
                .map(FuelRequestConverter::toDto).collect(Collectors.toList());
    }

    public List<FuelOilGreaseRequestDTO> getRequestsByMechanic(String mechanicName) {
        return requestRepository.findByMechanicName(mechanicName).stream()
                .map(FuelRequestConverter::toDto).collect(Collectors.toList());
    }

    public FuelOilGreaseRequestDTO getRequestById(Long id) {
        return requestRepository.findById(id)
                .map(FuelRequestConverter::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
    }
}