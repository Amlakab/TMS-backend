package com.amlakie.usermanagment.service; // Adjust package

import com.amlakie.usermanagment.entity.MaintenanceRequest;
import com.amlakie.usermanagment.entity.maintainance.RepairInfo; // Adjust package
import com.amlakie.usermanagment.entity.maintainance.WorksDoneLevel; // Adjust package
import com.amlakie.usermanagment.dto.maintainance.MaintenanceRecordDTO;
import com.amlakie.usermanagment.dto.maintainance.RepairDetailsDTO;
import com.amlakie.usermanagment.dto.maintainance.VehicleDetailsDTO;
import com.amlakie.usermanagment.entity.maintainance.MaintenanceRecord; // Adjust package
import com.amlakie.usermanagment.repository.MaintenanceRecordRepository; // Adjust package
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    // 1. Inject the MaintenanceRequestService
    private final MaintenanceRequestService maintenanceRequestService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // Expects "YYYY-MM-DD"

    @Transactional
    public MaintenanceRecordDTO createMaintenanceRecord(MaintenanceRecordDTO dto) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setPlateNumber(dto.getPlateNumber());

        if (dto.getVehicleDetails() != null) {
            record.setVehicleType(dto.getVehicleDetails().getType());
            record.setVehicleKm(dto.getVehicleDetails().getKm());
            record.setVehicleChassisNumber(dto.getVehicleDetails().getChassisNumber());
        }

        record.setDriverDescription(dto.getDriverDescription());
        record.setMechanicalRepair(convertToRepairInfo(dto.getMechanicalRepair()));
        record.setElectricalRepair(convertToRepairInfo(dto.getElectricalRepair()));

        MaintenanceRecord savedRecord = maintenanceRecordRepository.save(record);
        log.info("Created maintenance record with ID: {}", savedRecord.getId());
        return convertToDTO(savedRecord);
    }

    /**
     * Fetches vehicle details by looking up the latest MaintenanceRequest for the given plate number.
     *
     * @param plateNumber The plate number of the vehicle.
     * @return An Optional containing the vehicle details if a corresponding request is found.
     */
    @Transactional(readOnly = true)
    public Optional<VehicleDetailsDTO> getVehicleDetailsByPlateNumber(String plateNumber) {
        log.debug("Attempting to fetch vehicle details for plate: {} from MaintenanceRequest table", plateNumber);

        // 2. Fetch the latest maintenance request for the given plate number
        Optional<MaintenanceRequest> latestRequestOpt = maintenanceRequestService.getLatestMaintenanceRequestByPlateNumber(plateNumber);

        // 3. If a request is found, map its details to the VehicleDetailsDTO
        return latestRequestOpt.map(request -> {
            VehicleDetailsDTO vehicleDetails = new VehicleDetailsDTO();
            vehicleDetails.setType(request.getVehicleType());
            // Map the kilometerReading from the entity to the 'km' field in the DTO
            vehicleDetails.setKm(request.getKilometerReading() != null ? String.valueOf(request.getKilometerReading()) : null);
            // MaintenanceRequest might not have a chassis number. If it does, map it here.
            // vehicleDetails.setChassisNumber(request.getChassisNumber());
            log.info("Found vehicle details in MaintenanceRequest for plate {}: Type={}, KM={}",
                    plateNumber, vehicleDetails.getType(), vehicleDetails.getKm());
            return vehicleDetails;
        });
    }

    private RepairInfo convertToRepairInfo(RepairDetailsDTO dto) {
        if (dto == null) {
            return null;
        }
        RepairInfo info = new RepairInfo();
        info.setDateOfReceipt(parseLocalDate(dto.getDateOfReceipt()));
        info.setDateStarted(parseLocalDate(dto.getDateStarted()));
        info.setDateFinished(parseLocalDate(dto.getDateFinished()));
        info.setDuration(dto.getDuration());
        info.setInspectorName(dto.getInspectorName());
        info.setTeamLeader(dto.getTeamLeader());

        if (dto.getWorksDoneLevel() != null && !dto.getWorksDoneLevel().trim().isEmpty()) {
            try {
                info.setWorksDoneLevel(WorksDoneLevel.valueOf(dto.getWorksDoneLevel().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid worksDoneLevel string from DTO: '{}'. Setting to null.", dto.getWorksDoneLevel(), e);
                info.setWorksDoneLevel(null); // Or handle as an error
            }
        } else {
            info.setWorksDoneLevel(null);
        }
        info.setWorksDoneDescription(dto.getWorksDoneDescription());
        return info;
    }

    private LocalDate parseLocalDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format for string: '{}'. Expected YYYY-MM-DD. Returning null.", dateString, e);
            return null; // Or throw a custom exception
        }
    }

    private MaintenanceRecordDTO convertToDTO(MaintenanceRecord entity) {
        if (entity == null) return null;
        MaintenanceRecordDTO dto = new MaintenanceRecordDTO();
        dto.setId(entity.getId());
        dto.setPlateNumber(entity.getPlateNumber());
        dto.setVehicleDetails(new VehicleDetailsDTO(
                entity.getVehicleType(),
                entity.getVehicleKm(),
                entity.getVehicleChassisNumber()
        ));
        dto.setDriverDescription(entity.getDriverDescription());
        dto.setMechanicalRepair(convertToRepairDetailsDTO(entity.getMechanicalRepair()));
        dto.setElectricalRepair(convertToRepairDetailsDTO(entity.getElectricalRepair()));
        return dto;
    }

    private RepairDetailsDTO convertToRepairDetailsDTO(RepairInfo info) {
        if (info == null) return null;
        return new RepairDetailsDTO(
                info.getDateOfReceipt() != null ? info.getDateOfReceipt().format(DATE_FORMATTER) : null,
                info.getDateStarted() != null ? info.getDateStarted().format(DATE_FORMATTER) : null,
                info.getDateFinished() != null ? info.getDateFinished().format(DATE_FORMATTER) : null,
                info.getDuration(),
                info.getInspectorName(),
                info.getTeamLeader(),
                info.getWorksDoneLevel() != null ? info.getWorksDoneLevel().name().toLowerCase() : "",
                info.getWorksDoneDescription()
        );
    }
}