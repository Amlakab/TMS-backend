package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.attendance.CarAttendanceResponseDTO;
import com.amlakie.usermanagment.dto.attendance.EveningDepartureRequestDTO;
import com.amlakie.usermanagment.dto.attendance.MorningArrivalRequestDTO;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.Vehicle;
import com.amlakie.usermanagment.entity.attendance.CarAttendance;
import com.amlakie.usermanagment.exception.ResourceNotFoundException; // Custom exception
import com.amlakie.usermanagment.exception.ValidationException; // Custom exception
import com.amlakie.usermanagment.repository.CarAttendanceRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List; // Import List
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors

@Service
public class CarAttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(CarAttendanceService.class); // SLF4J Logger

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
    public CarAttendanceResponseDTO recordMorningArrival(MorningArrivalRequestDTO dto) {
        Vehicle vehicle = findVehicle(dto.getPlateNumber(), dto.getVehicleType());
        LocalDate today = LocalDate.now();

        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        Optional<CarAttendance> existingTodayRecord = carAttendanceRepository
                .findByVehicleDetailsAndDateAndEveningKmIsNull(vehicle.getId(), vehicleTypeDiscriminator, today);

        if (existingTodayRecord.isPresent()) {
            throw new ValidationException("Morning arrival already recorded for vehicle " +
                    dto.getPlateNumber() + " on " + today);
        }

        CarAttendance newAttendance = new CarAttendance();
        newAttendance.setVehicle(vehicle);
        newAttendance.setDate(today);
        newAttendance.setMorningKm(dto.getMorningKm());
        newAttendance.setFuelLitersAdded(dto.getFuelLitersAdded());

        Optional<CarAttendance> previousAttendanceOpt = carAttendanceRepository
                .findTopByVehicleDetailsAndDateBeforeOrderByDateDesc(vehicle.getId(), vehicleTypeDiscriminator, today);

        if (previousAttendanceOpt.isPresent()) {
            CarAttendance previousAttendance = previousAttendanceOpt.get();
            if (previousAttendance.getEveningKm() != null && dto.getMorningKm() != null) {
                if (dto.getMorningKm() >= previousAttendance.getEveningKm()) {
                    newAttendance.setOvernightKmDifferenceFromPrevious(
                            dto.getMorningKm() - previousAttendance.getEveningKm()
                    );
                } else {
                    // Log a warning if morning KM is less than previous evening KM
                    logger.warn("Morning KM ({}) is less than previous evening KM ({}) for vehicle {}. Setting overnight difference to null.",
                            dto.getMorningKm(), previousAttendance.getEveningKm(), vehicle.getPlateNumber());
                    // Business decision: Set to null, 0, or throw an error. Setting to null here.
                    newAttendance.setOvernightKmDifferenceFromPrevious(null);
                }
            } else {
                // Previous evening KM was null, or current morning KM is null. Cannot calculate.
                newAttendance.setOvernightKmDifferenceFromPrevious(null);
            }
        } else {
            // No previous attendance record found, so no overnight difference.
            newAttendance.setOvernightKmDifferenceFromPrevious(null);
        }

        CarAttendance savedAttendance = carAttendanceRepository.save(newAttendance);

        if (dto.getMorningKm() != null) {
            vehicle.setCurrentKm(dto.getMorningKm());
            saveVehicle(vehicle);
        }

        return mapToResponseDTO(savedAttendance);
    }

    @Transactional
    public CarAttendanceResponseDTO recordEveningDeparture(Long attendanceId, EveningDepartureRequestDTO dto) {
        CarAttendance attendance = carAttendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("CarAttendance record not found with ID: " + attendanceId));

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
        if (dto.getFuelLitersAdded() != null) {
            double currentFuel = attendance.getFuelLitersAdded() != null ? attendance.getFuelLitersAdded() : 0.0;
            attendance.setFuelLitersAdded(currentFuel + dto.getFuelLitersAdded());
        }

        Vehicle vehicle = attendance.getVehicle();
        if (vehicle != null && dto.getEveningKm() != null) {
            vehicle.setCurrentKm(dto.getEveningKm());
            saveVehicle(vehicle);
        }

        // The @PreUpdate in CarAttendance entity should calculate dailyKmDifference now
        CarAttendance updatedAttendance = carAttendanceRepository.save(attendance);
        return mapToResponseDTO(updatedAttendance);
    }

    @Transactional(readOnly = true)
    public CarAttendanceResponseDTO findTodaysMorningArrivalRecord(String plateNumber, String vehicleType) {
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        LocalDate today = LocalDate.now();
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        return carAttendanceRepository.findByVehicleDetailsAndDateAndEveningKmIsNull(vehicle.getId(), vehicleTypeDiscriminator, today)
                .map(this::mapToResponseDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public CarAttendanceResponseDTO findLastEveningDepartureRecord(String plateNumber, String vehicleType) {
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        LocalDate today = LocalDate.now(); // Records before today
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        List<CarAttendance> departures = carAttendanceRepository
                .findCompletedDeparturesBeforeDateOrdered(vehicle.getId(), vehicleTypeDiscriminator, today);

        if (!departures.isEmpty()) {
            // The first element is the "top" one due to the ORDER BY clause
            return mapToResponseDTO(departures.get(0));
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Optional<CarAttendanceResponseDTO> getAttendanceById(Long id) {
        return carAttendanceRepository.findById(id)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<CarAttendanceResponseDTO> getAllAttendanceRecords() {
        // Consider pagination for production environments if this list can grow very large
        return carAttendanceRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CarAttendanceResponseDTO> getAttendanceForVehicle(String plateNumber, String vehicleType) {
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        return carAttendanceRepository.findAllByVehicleDetailsOrderByDateDesc(vehicle.getId(), vehicleTypeDiscriminator).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
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

    private CarAttendanceResponseDTO mapToResponseDTO(CarAttendance entity) {
        if (entity == null) {
            return null;
        }
        CarAttendanceResponseDTO dto = new CarAttendanceResponseDTO();
        dto.setId(entity.getId());

        Vehicle vehicle = entity.getVehicle();
        if (vehicle != null) {
            dto.setVehiclePlateNumber(vehicle.getPlateNumber());
            // Use the more reliable discriminator from the helper method,
            // or directly from entity if CarAttendance stores its vehicle's discriminator string.
            // For now, instanceof is robust.
            if (vehicle instanceof Car) {
                dto.setVehicleType(VEHICLE_TYPE_CAR);
            } else if (vehicle instanceof OrganizationCar) {
                dto.setVehicleType(VEHICLE_TYPE_ORGANIZATION_CAR);
            }

            // Ensure these are inside the null check for 'vehicle'
            // Uncommented driverName mapping as requested
            // Assuming getDriverName() exists on Vehicle or its subtypes
            dto.setDriverName(vehicle.getDriverName());
            dto.setKmPerLiter(vehicle.getKmPerLiter());

        } else {
            // Optional: Log a warning if a CarAttendance record has no associated vehicle
            logger.warn("CarAttendance record with ID {} has no associated vehicle.", entity.getId());
            // You might want to set default values or null for vehicle-dependent fields
            dto.setVehiclePlateNumber(null);
            dto.setVehicleType(null);
            dto.setDriverName(null);
            dto.setKmPerLiter(0.0F);
        }


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