package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.TransferInfoRequestDTO;
import com.amlakie.usermanagment.dto.TransferInfoResponseDTO;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.entity.TransferInfo;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.repository.TransferInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransferInfoServiceImpl implements TransferInfoService {

    private final TransferInfoRepository transferInfoRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;

    @Autowired
    public TransferInfoServiceImpl(TransferInfoRepository transferInfoRepository,
                                   AssignmentHistoryRepository assignmentHistoryRepository) {
        this.transferInfoRepository = transferInfoRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
    }

    @Override
    public TransferInfoResponseDTO createTransfer(TransferInfoRequestDTO transferInfoDTO) {
        // Find the assignment history record
        AssignmentHistory assignmentHistory = assignmentHistoryRepository.findById(transferInfoDTO.getAssignmentHistoryId())
                .orElseThrow(() -> new RuntimeException("Assignment history not found"));

        // Convert DTO to Entity
        TransferInfo transferInfo = mapDTOToEntity(transferInfoDTO, assignmentHistory);

        // Save the entity
        TransferInfo savedTransfer = transferInfoRepository.save(transferInfo);

        // Convert Entity back to DTO for response
        return mapEntityToDTO(savedTransfer);
    }

    private TransferInfo mapDTOToEntity(TransferInfoRequestDTO dto, AssignmentHistory assignmentHistory) {
        TransferInfo transferInfo = new TransferInfo();
        transferInfo.setTransferDate(dto.getTransferDate());
        transferInfo.setTransferNumber(dto.getTransferNumber());
        transferInfo.setAssignmentHistory(assignmentHistory);
        transferInfo.setOldKmReading(dto.getOldKmReading());
        transferInfo.setDesignatedOfficial(dto.getDesignatedOfficial());
        transferInfo.setDriverName(dto.getDriverName());
        transferInfo.setTransferReason(dto.getTransferReason());
        transferInfo.setOldFuelLiters(dto.getOldFuelLiters());
        transferInfo.setNewKmReading(dto.getNewKmReading());
        transferInfo.setCurrentDesignatedOfficial(dto.getCurrentDesignatedOfficial());
        transferInfo.setNewFuelLiters(dto.getNewFuelLiters());
        transferInfo.setVerifyingBodyName(dto.getVerifyingBodyName());
        transferInfo.setAuthorizingOfficerName(dto.getAuthorizingOfficerName());

        return transferInfo;
    }

    private TransferInfoResponseDTO mapEntityToDTO(TransferInfo entity) {
        TransferInfoResponseDTO dto = new TransferInfoResponseDTO();
        dto.setTransferId(entity.getTransferId());
        dto.setTransferDate(entity.getTransferDate());
        dto.setTransferNumber(entity.getTransferNumber());
        dto.setAssignmentHistoryId(entity.getAssignmentHistory().getId());
        dto.setOldKmReading(entity.getOldKmReading());
        dto.setDesignatedOfficial(entity.getDesignatedOfficial());
        dto.setDriverName(entity.getDriverName());
        dto.setTransferReason(entity.getTransferReason());
        dto.setOldFuelLiters(entity.getOldFuelLiters());
        dto.setNewKmReading(entity.getNewKmReading());
        dto.setCurrentDesignatedOfficial(entity.getCurrentDesignatedOfficial());
        dto.setNewFuelLiters(entity.getNewFuelLiters());
        dto.setVerifyingBodyName(entity.getVerifyingBodyName());
        dto.setAuthorizingOfficerName(entity.getAuthorizingOfficerName());

        return dto;
    }
}