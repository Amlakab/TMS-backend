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
import com.amlakie.usermanagment.dto.attendance.ServiceDueVehicleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final EmailSentService emailSentService;

    @Value("${vehicle.service.reminder-km-threshold:4600}") // Inject from properties with a default
    private int serviceReminderKmThreshold;

    @Autowired
    public CarAttendanceService(CarAttendanceRepository carAttendanceRepository,
                                CarRepository carRepository,
                                OrganizationCarRepository organizationCarRepository,
                                EmailSentService emailSentService) {
        this.carAttendanceRepository = carAttendanceRepository;
        this.carRepository = carRepository;
        this.organizationCarRepository = organizationCarRepository;
        this.emailSentService = emailSentService;
    }

    @Transactional
    public CarAttendanceResponseDTO recordMorningArrival(MorningArrivalRequestDTO dto) {
        logger.info("Recording morning arrival: {}", dto);

        Vehicle vehicle = findVehicle(dto.getPlateNumber(), dto.getVehicleType());
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        LocalDate today = LocalDate.now();
        if (carAttendanceRepository.findByVehicleDetailsAndDate(vehicle.getId(), vehicleTypeDiscriminator, today).isPresent()) {
            throw new ValidationException("Vehicle " + dto.getPlateNumber() + " already checked in today.");
        }

        CarAttendance attendance = new CarAttendance();
        attendance.setVehicle(vehicle);
        attendance.setDate(today);
        attendance.setMorningKm(dto.getMorningKm());
        attendance.setVehicleTypeDiscriminator(vehicleTypeDiscriminator);

        // BEST PRACTICE: Calculate everything BEFORE saving
        calculateOvernightDifference(attendance, vehicle.getId(), vehicleTypeDiscriminator, today);

        // Save the fully populated object ONCE
        CarAttendance savedAttendance = carAttendanceRepository.save(attendance);

        // Capture the previous KM value BEFORE updating the vehicle
        Double previousTotalKm = vehicle.getCurrentKm();

        // Update the vehicle's main KM reading
        if (dto.getMorningKm() != null && (previousTotalKm == null || dto.getMorningKm() >= previousTotalKm)) {
            vehicle.setCurrentKm(dto.getMorningKm());
            saveVehicle(vehicle);
        }

        // Pass the previous KM value to the reminder method for an accurate calculation
        checkAndSendKilometerBasedReminder(savedAttendance, previousTotalKm);
        return mapToResponseDTO(savedAttendance);
    }

    @Transactional(readOnly = true)
    public List<ServiceDueVehicleDTO> findVehiclesDueForService() {
        logger.info("Finding vehicles due for service with a threshold of {}", serviceReminderKmThreshold);

        // Note: You must add the 'findWithServiceDue' method to your repository interface.
        // This query efficiently finds cars where (totalKm - lastServiceKm) >= threshold.
        List<OrganizationCar> orgCarsDue = organizationCarRepository.findWithServiceDue(serviceReminderKmThreshold);

        // You can uncomment the lines below if your 'Car' entity also has service tracking.
        // List<Car> carsDue = carRepository.findWithServiceDue(serviceReminderKmThreshold);

        // Map the results to the DTO
        List<ServiceDueVehicleDTO> serviceDueList = orgCarsDue.stream()
                .map(this::mapOrgCarToServiceDueDTO)
                .collect(Collectors.toList());

        // carsDue.stream()
        //     .map(this::mapCarToServiceDueDTO) // You would need to create this helper method for Car
        //     .forEach(serviceDueList::add);

        return serviceDueList;
    }

    private ServiceDueVehicleDTO mapOrgCarToServiceDueDTO(OrganizationCar car) {
        ServiceDueVehicleDTO dto = new ServiceDueVehicleDTO();
        dto.setPlateNumber(car.getPlateNumber());
        dto.setVehicleType(VEHICLE_TYPE_ORGANIZATION_CAR);
        dto.setDriverName(car.getDriverName());
        dto.setCurrentKm(car.getTotalKm());
        dto.setLastServiceKm(car.getLastServiceKm());

        if (car.getTotalKm() != null && car.getLastServiceKm() != null) {
            dto.setKmSinceLastService(car.getTotalKm() - car.getLastServiceKm());
        }
        return dto;
    }

    @Transactional
    public CarAttendanceResponseDTO recordEveningDeparture(Long attendanceId, EveningDepartureRequestDTO dto) {
        logger.info("Recording evening departure for attendance ID {}: {}", attendanceId, dto);
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
            attendance.setFuelLitersAdded(
                    (attendance.getFuelLitersAdded() != null ? attendance.getFuelLitersAdded() : 0)
                            + dto.getFuelLitersAdded()
            );
        }

        Vehicle vehicle = attendance.getVehicle();
        if (vehicle != null) {
            vehicle.setCurrentKm(dto.getEveningKm());
            saveVehicle(vehicle);
        }

        CarAttendance updatedAttendance = carAttendanceRepository.save(attendance);
        return mapToResponseDTO(updatedAttendance);
    }

    @Transactional
    public FuelEntryResponseDTO recordFuelEntry(FuelEntryRequestDTO dto) {
        logger.info("Processing fuel entry for plate: {}, date: {}", dto.getVehiclePlateNumber(), dto.getFuelingDate());

        Vehicle vehicle = findVehicle(dto.getVehiclePlateNumber(), dto.getVehicleType());
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        LocalDate entryDate;
        try {
            entryDate = LocalDate.parse(dto.getFuelingDate());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid fueling date format. Please use YYYY-MM-DD.");
        }

        CarAttendance attendanceRecord = carAttendanceRepository
                .findByVehicleDetailsAndDate(vehicle.getId(), vehicleTypeDiscriminator, entryDate)
                .orElseThrow(() -> new ValidationException("No existing attendance record found for vehicle " +
                        dto.getVehiclePlateNumber() + " on " + entryDate + ". Fuel entry requires a prior check-in."));

        double currentFuel = attendanceRecord.getFuelLitersAdded() != null ? attendanceRecord.getFuelLitersAdded() : 0.0;
        attendanceRecord.setFuelLitersAdded(currentFuel + dto.getLitersAdded());

        if (dto.getKmAtFueling() != null) {
            attendanceRecord.setKmAtFueling(dto.getKmAtFueling());
            if (vehicle.getCurrentKm() == null || dto.getKmAtFueling() > vehicle.getCurrentKm()) {
                vehicle.setCurrentKm(dto.getKmAtFueling());
                saveVehicle(vehicle);
            }
        }

        attendanceRecord.setUpdatedAt(LocalDateTime.now());
        CarAttendance savedAttendance = carAttendanceRepository.save(attendanceRecord);
        return mapToFuelEntryResponseDTO(savedAttendance);
    }

    @Transactional(readOnly = true)
    public CarAttendanceResponseDTO findTodaysMorningArrivalRecord(String plateNumber, String vehicleType) {
        logger.info("Finding today's morning arrival for plate: {}, type: {}", plateNumber, vehicleType);
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        LocalDate today = LocalDate.now();
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        return carAttendanceRepository
                .findByVehicleDetailsAndDateAndEveningKmIsNull(vehicle.getId(), vehicleTypeDiscriminator, today)
                .map(this::mapToResponseDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public CarAttendanceResponseDTO findLastEveningDepartureRecord(String plateNumber, String vehicleType) {
        logger.info("Finding last evening departure for plate: {}, type: {}", plateNumber, vehicleType);
        Vehicle vehicle = findVehicle(plateNumber, vehicleType);
        LocalDate today = LocalDate.now();
        String vehicleTypeDiscriminator = getVehicleTypeDiscriminator(vehicle);

        return carAttendanceRepository
                .findTopByVehicleDetailsAndDateBeforeOrderByDateDesc(vehicle.getId(), vehicleTypeDiscriminator, today)
                .map(this::mapToResponseDTO)
                .orElse(null);
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

        return carAttendanceRepository.findAllByVehicleDetailsOrderByDateDesc(vehicle.getId(), vehicleTypeDiscriminator).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    private void calculateOvernightDifference(CarAttendance currentAttendance, Long vehicleId, String discriminator, LocalDate today) {
        LocalDate previousDate = today.minusDays(1);
        carAttendanceRepository.findByVehicleDetailsAndDate(vehicleId, discriminator, previousDate)
                .ifPresent(previousAttendance -> {
                    if (previousAttendance.getEveningKm() != null && currentAttendance.getMorningKm() != null) {
                        double overnightDiff = currentAttendance.getMorningKm() - previousAttendance.getEveningKm();
                        currentAttendance.setOvernightKmDifferenceFromPrevious(overnightDiff);
                    }
                });
    }

    private void checkAndSendKilometerBasedReminder(CarAttendance attendance, Double previousTotalKm) {
        Vehicle vehicle = attendance.getVehicle();
        Double morningKm = attendance.getMorningKm();

        if (vehicle instanceof OrganizationCar orgCar) {
            Double lastServiceKm = orgCar.getLastServiceKm();
            // Use the explicit previousTotalKm for a reliable calculation
            Double referenceKm = (lastServiceKm != null && lastServiceKm > 0) ? lastServiceKm : previousTotalKm;

            if (referenceKm != null && morningKm != null) {
                double diff = morningKm - referenceKm;
                // Add this debug line to see the exact values being used in the calculation
                logger.info("DEBUG: Checking service reminder for {}. morningKm={}, referenceKm={}, diff={}, threshold={}", orgCar.getPlateNumber(), morningKm, referenceKm, diff, serviceReminderKmThreshold);
                if (diff >= serviceReminderKmThreshold) { // Check if the difference meets the configured threshold
                    logger.info("Service interval reached for {}. Sending reminder email.", orgCar.getPlateNumber());
                    emailSentService.sendSimpleMessage(
                            orgCar.getOwnerEmail(),
                            "Service Due Reminder",
                            "Your car with plate number" + orgCar.getPlateNumber() +
                                    " has reached its service interval. Please come and take your car for service."+
                                    "INSA TMS"
                    );
                }
            }
        }
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
        }
    }

    private String getVehicleTypeDiscriminator(Vehicle vehicle) {
        if (vehicle instanceof Car) {
            return VEHICLE_TYPE_CAR;
        } else if (vehicle instanceof OrganizationCar) {
            return VEHICLE_TYPE_ORGANIZATION_CAR;
        }
        throw new IllegalArgumentException("Unknown vehicle instance type: " + vehicle.getClass().getName());
    }

    private FuelEntryResponseDTO mapToFuelEntryResponseDTO(CarAttendance entity) {
        FuelEntryResponseDTO dto = new FuelEntryResponseDTO();
        dto.setId(entity.getId());
        dto.setVehiclePlateNumber(entity.getVehicle().getPlateNumber());
        dto.setVehicleType(getVehicleTypeDiscriminator(entity.getVehicle()));
        dto.setLitersAdded(entity.getFuelLitersAdded());
        dto.setKmAtFueling(entity.getKmAtFueling());
        dto.setFuelingDate(entity.getDate().toString());
        dto.setMessage("Fuel entry updated successfully.");
        return dto;
    }

    private CarAttendanceResponseDTO mapToResponseDTO(CarAttendance entity) {
        if (entity == null) return null;
        CarAttendanceResponseDTO dto = new CarAttendanceResponseDTO();
        dto.setId(entity.getId());

        Vehicle vehicle = entity.getVehicle();
        if (vehicle != null) {
            dto.setVehiclePlateNumber(vehicle.getPlateNumber());
            dto.setVehicleType(getVehicleTypeDiscriminator(vehicle));
            dto.setDriverName(vehicle.getDriverName());
            dto.setKmPerLiter(vehicle.getKmPerLiter());
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