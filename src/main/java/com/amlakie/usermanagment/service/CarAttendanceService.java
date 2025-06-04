package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.attendance.*;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.Vehicle;
import com.amlakie.usermanagment.entity.attendance.CarAttendance;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.exception.ValidationException;
import com.amlakie.usermanagment.repository.CarAttendanceRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarAttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(CarAttendanceService.class);

    private static final String VEHICLE_TYPE_CAR = "CAR";
    private static final String VEHICLE_TYPE_ORGANIZATION_CAR = "ORGANIZATION_CAR";

    private final CarAttendanceRepository carAttendanceRepository;
    private final CarRepository carRepository;
    private final OrganizationCarRepository organizationCarRepository;

    public CarAttendanceService(CarAttendanceRepository carAttendanceRepository,
                                CarRepository carRepository,
                                OrganizationCarRepository organizationCarRepository) {
        this.carAttendanceRepository = carAttendanceRepository;
        this.carRepository = carRepository;
        this.organizationCarRepository = organizationCarRepository;
    }

    @Transactional
    public CarAttendanceResponseDTO recordEveningDeparture(Long attendanceId, EveningDepartureRequestDTO dto) {
        logger.info("Recording evening departure for attendance ID {}: {}", attendanceId, dto);
        CarAttendance attendance = carAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("CarAttendance record not found with ID: " + attendanceId));

        // Basic validation - adjust as needed for your specific business rules.
        if (attendance.getEveningKm() != null) {
            throw new ValidationException("Evening departure already recorded for this attendance.");
        }
        if (dto.getEveningKm() == null) {
            throw new ValidationException("Evening KM cannot be null for departure.");
        }
        if (attendance.getMorningKm() != null && dto.getEveningKm() < attendance.getMorningKm()) {
            throw new ValidationException("Evening KM (" + dto.getEveningKm() +
                    ") cannot be less than morning KM (" + attendance.getMorningKm() + ").");
        }

        attendance.setEveningKm(dto.getEveningKm());
        //If fuel is added in the evening
        if (dto.getFuelLitersAdded() != null) {
            attendance.setFuelLitersAdded(
                    (attendance.getFuelLitersAdded() != null ? attendance.getFuelLitersAdded() : 0)
                            + dto.getFuelLitersAdded()
            );
        }

        Vehicle vehicle = attendance.getVehicle();
        if (vehicle != null) {
            vehicle.setCurrentKm(dto.getEveningKm());
            saveVehicle(vehicle); // Assume you have a method to save the vehicle
        }

        CarAttendance updatedAttendance = carAttendanceRepository.save(attendance);
        return mapToResponseDTO(updatedAttendance); // Assuming you have a mapping method
    }

    @Transactional(readOnly = true)
    public CarAttendanceResponseDTO findTodaysMorningArrivalRecord(String plateNumber, String vehicleType) {
        logger.info("Finding today's morning arrival for plate: {}, type: {}", plateNumber, vehicleType);
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        LocalDate today = LocalDate.now();
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle); // Get discriminator

        return carAttendanceRepository
                .findByVehicleDetailsAndDateAndEveningKmIsNull(vehicle.getId(), vehicleTypeDiscriminator, today)
                .map(this::mapToResponseDTO)
                .orElse(null); // Or throw an exception if you prefer
    }
    @Transactional
    public CarAttendanceResponseDTO recordMorningArrival(MorningArrivalRequestDTO dto) {
        logger.info("Recording morning arrival: {}", dto);

        // 1. Find the Vehicle
        Vehicle vehicle = findVehicle(dto.getPlateNumber(), dto.getVehicleType());
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        // 2. Validate if already checked in today (optional, based on your requirements)
        LocalDate today = LocalDate.now();
        Optional<CarAttendance> existingAttendance = carAttendanceRepository.findByVehicleDetailsAndDate(vehicle.getId(), vehicleTypeDiscriminator, today);
        if (existingAttendance.isPresent()) {
            throw new ValidationException("Vehicle " + dto.getPlateNumber() + " already checked in today.");
        }

        // 3. Create new CarAttendance record
        CarAttendance attendance = new CarAttendance();
        attendance.setVehicle(vehicle);
        attendance.setDate(today);
        attendance.setMorningKm(dto.getMorningKm());
        attendance.setVehicleTypeDiscriminator(vehicleTypeDiscriminator); // Set the discriminator
        // You might set other fields like driver, etc. from the DTO if needed.

        // 4. Save the record
        CarAttendance savedAttendance = carAttendanceRepository.save(attendance);

        // 5. Update Vehicle's current KM (if the morning KM is the latest)
        if (dto.getMorningKm() != null && (vehicle.getCurrentKm() == null || dto.getMorningKm() >= vehicle.getCurrentKm())) {
            vehicle.setCurrentKm(dto.getMorningKm());
            saveVehicle(vehicle); // Call method to save or update the vehicle
        }
// After saving the new attendance record
LocalDate previousDate = today.minusDays(1);
Optional<CarAttendance> previousAttendanceOpt = carAttendanceRepository
    .findByVehicleDetailsAndDate(vehicle.getId(), vehicleTypeDiscriminator, previousDate);

if (previousAttendanceOpt.isPresent()) {
    CarAttendance previousAttendance = previousAttendanceOpt.get();
    if (previousAttendance.getEveningKm() != null && attendance.getMorningKm() != null) {
        double overnightDiff = attendance.getMorningKm() - previousAttendance.getEveningKm();
        attendance.setOvernightKmDifferenceFromPrevious(overnightDiff);
        carAttendanceRepository.save(attendance); // Save the updated value
    }
}
        return mapToResponseDTO(savedAttendance);
    }

    @Transactional(readOnly = true)
    public CarAttendanceResponseDTO findLastEveningDepartureRecord(String plateNumber, String vehicleType) {
        logger.info("Finding last evening departure for plate: {}, type: {}", plateNumber, vehicleType);
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        LocalDate today = LocalDate.now(); // We are interested in records before today
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        // Assuming you have a repository method to find the last departure
        Optional<CarAttendance> lastDepartureOpt = carAttendanceRepository
                .findTopByVehicleDetailsAndDateBeforeOrderByDateDesc(vehicle.getId(), vehicleTypeDiscriminator, today);

        return lastDepartureOpt.map(this::mapToResponseDTO).orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<CarAttendanceResponseDTO> getAttendanceById(Long id) {
        logger.info("Getting attendance by ID: {}", id);
        return carAttendanceRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<CarAttendanceResponseDTO> getAllAttendanceRecords() {
        logger.info("Getting all attendance records.");
        return carAttendanceRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarAttendanceResponseDTO> getAttendanceForVehicle(String plateNumber, String vehicleType) {
        logger.info("Getting attendance for vehicle plate: {}, type: {}", plateNumber, vehicleType);
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        // Assuming you have a repository method to find by vehicle
        return carAttendanceRepository.findAllByVehicleDetailsOrderByDateDesc(vehicle.getId(), vehicleTypeDiscriminator).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Records a fuel entry for a vehicle on a specific date, updating an existing record's fuel and potentially KM.
     * Throws a ValidationException if no attendance record exists for the given vehicle and date.
     *
     * @param dto The DTO containing fuel entry details.
     * @return A FuelEntryResponseDTO indicating the result.
     */
    @Transactional
    public FuelEntryResponseDTO recordFuelEntry(FuelEntryRequestDTO dto) {
        logger.info("Processing fuel entry (update) for plate: {}, type: {}, date: {}, liters: {}, km: {}",
                dto.getVehiclePlateNumber(), dto.getVehicleType(), dto.getFuelingDate(), dto.getLitersAdded(), dto.getKmAtFueling());

        // 1. Find the Vehicle
        Vehicle vehicle = findVehicle(dto.getVehiclePlateNumber(), dto.getVehicleType());
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        // 2. Parse the date
        LocalDate entryDate;
        try {
            entryDate = LocalDate.parse(dto.getFuelingDate());
        } catch (DateTimeParseException dtpe) {
            logger.warn("Invalid fueling date format received: {}", dto.getFuelingDate());
            throw new ValidationException("Invalid fueling date format. Please use YYYY-MM-DD.");
        }

        // 3. Look for an existing record
        Optional<CarAttendance> attendanceOpt = carAttendanceRepository
                .findByVehicleDetailsAndDate(vehicle.getId(), vehicleTypeDiscriminator, entryDate);

        if (attendanceOpt.isEmpty()) {
            logger.warn("No attendance record found for plate: {}, type: {}, date: {}. Cannot add fuel.",
                    dto.getVehiclePlateNumber(), dto.getVehicleType(), entryDate);
            throw new ValidationException("No existing attendance record found for vehicle " + dto.getVehiclePlateNumber() +
                    " on " + entryDate + ". Fuel entry requires a prior check-in.");
        }

        CarAttendance attendanceRecord = attendanceOpt.get();
        logger.info("Found existing attendance record ID {} for date {} to update with fuel.", attendanceRecord.getId(), entryDate);

        // 4. Update Fuel Liters (cumulative)
        double currentFuel = attendanceRecord.getFuelLitersAdded() != null ? attendanceRecord.getFuelLitersAdded() : 0.0;
        attendanceRecord.setFuelLitersAdded(currentFuel + dto.getLitersAdded());
        logger.debug("Updated fuel liters for record ID {} to {}", attendanceRecord.getId(), attendanceRecord.getFuelLitersAdded());

        if (dto.getKmAtFueling() != null) {
            attendanceRecord.setKmAtFueling(dto.getKmAtFueling()); // <-- CORRECT
            // Optionally update vehicle current KM if needed
            if (vehicle.getCurrentKm() == null || dto.getKmAtFueling() > vehicle.getCurrentKm()) {
                vehicle.setCurrentKm(dto.getKmAtFueling());
                saveVehicle(vehicle);
            }
            logger.debug("Updated attendance record ID {} with kmAtFueling: {}, and vehicle {} current KM to {}",
                    attendanceRecord.getId(), dto.getKmAtFueling(), vehicle.getPlateNumber(), vehicle.getCurrentKm());
        }
        // If the new KM is lower than the existing evening KM, it's likely an error or not a proper end-of-day reading.
        else if (dto.getKmAtFueling() != null) {
            logger.warn("Received kmAtFueling {} for record ID {}, but it's not a valid evening KM reading (less than or equal to existing eveningKm or morningKm).",
                    dto.getKmAtFueling(), attendanceRecord.getId());
            // Decide how to handle this: You might throw a ValidationException, ignore it, or have different logic.
            // For now, I'm just logging a warning and *not* updating the eveningKm on the attendance record.
        }

        // 6. Save the updated Attendance record
        attendanceRecord.setUpdatedAt(LocalDateTime.now());
        CarAttendance savedAttendance = carAttendanceRepository.save(attendanceRecord);
        logger.info("Fuel entry successfully updated in attendance record ID {}", savedAttendance.getId());

        // 7. Map to Response DTO
        return mapToFuelEntryResponseDTO(savedAttendance, dto);
    }

    /**
     * New mapping method for FuelEntryResponseDTO.
     * Maps data from the saved CarAttendance entity and the original request DTO.
     *
     * @param entity The saved CarAttendance entity.
     * @param requestDto The original FuelEntryRequestDTO.
     * @return The mapped FuelEntryResponseDTO.
     */
    private FuelEntryResponseDTO mapToFuelEntryResponseDTO(CarAttendance entity, FuelEntryRequestDTO requestDto) {
        FuelEntryResponseDTO dto = new FuelEntryResponseDTO();
        dto.setId(entity.getId());
        dto.setVehiclePlateNumber(entity.getVehicle().getPlateNumber());
        dto.setVehicleType(getVehicleTypeDiscriminator(entity.getVehicle()));
        dto.setLitersAdded(entity.getFuelLitersAdded()); // Cumulative
        dto.setKmAtFueling(entity.getEveningKm()); // Using Evening KM as the updated KM
        dto.setFuelingDate(requestDto.getFuelingDate());
        dto.setMessage("Fuel entry updated successfully.");
        return dto;
    }

    private String getVehicleTypeDiscriminator(Vehicle vehicle) {
        if (vehicle instanceof Car) {
            return VEHICLE_TYPE_CAR;
        } else if (vehicle instanceof OrganizationCar) {
            return VEHICLE_TYPE_ORGANIZATION_CAR;
        }
        // This should ideally not be reached if findVehicle works correctly and vehicle is always a known subtype
        logger.error("Unknown vehicle instance type encountered: {}", vehicle.getClass().getName());
        throw new IllegalArgumentException("Unknown vehicle instance type: " + vehicle.getClass().getName());
    }

    private Vehicle findVehicle(String plateNumber, String vehicleTypeInput) {
        if (VEHICLE_TYPE_CAR.equalsIgnoreCase(vehicleTypeInput)) {
            return carRepository.findByPlateNumber(plateNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Car not found with plate number: " + plateNumber));
        } else if (VEHICLE_TYPE_ORGANIZATION_CAR.equalsIgnoreCase(vehicleTypeInput)) {
            return organizationCarRepository.findByPlateNumber(plateNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("OrganizationCar not found with plate number: " + plateNumber));
        } else {
            throw new ValidationException("Invalid vehicle type provided: " + vehicleTypeInput);
        }
    }

    private void saveVehicle(Vehicle vehicle) {
        if (vehicle instanceof Car) {
            carRepository.save((Car) vehicle);
        } else if (vehicle instanceof OrganizationCar) {
            organizationCarRepository.save((OrganizationCar) vehicle);
        } else {
            logger.warn("Attempted to save an unknown vehicle subtype: {}", vehicle.getClass().getName());
            // Or throw an exception if this state is not expected
            // throw new IllegalArgumentException("Cannot save unknown vehicle subtype: " + vehicle.getClass().getName());
        }
    }

    // Existing mapping method for CarAttendanceResponseDTO
    private CarAttendanceResponseDTO mapToResponseDTO(CarAttendance entity) {
        if (entity == null) {
            return null;
        }
        CarAttendanceResponseDTO dto = new CarAttendanceResponseDTO();
        dto.setId(entity.getId());

        Vehicle vehicle = entity.getVehicle();
        if (vehicle != null) {
            dto.setVehiclePlateNumber(vehicle.getPlateNumber());
            dto.setVehicleType(getVehicleTypeDiscriminator(vehicle));
            dto.setDriverName(vehicle.getDriverName());
            dto.setKmPerLiter(vehicle.getKmPerLiter());

        } else {
            logger.warn("CarAttendance record with ID {} has no associated vehicle.", entity.getId());
            dto.setVehiclePlateNumber(null);
            dto.setVehicleType(null);
            dto.setDriverName(null);
            dto.setKmPerLiter(0.0F);
        }
        dto.setKmAtFueling(entity.getKmAtFueling());
        dto.setMorningKm(entity.getMorningKm());
        dto.setEveningKm(entity.getEveningKm());
        dto.setDailyKmDifference(entity.getDailyKmDifference());
        dto.setOvernightKmDifferenceFromPrevious(entity.getOvernightKmDifferenceFromPrevious());
        dto.setFuelLitersAdded(entity.getFuelLitersAdded());
        dto.setKmPerLiterCalculated(entity.getKmPerLiterCalculated());
        dto.setDate(entity.getDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}