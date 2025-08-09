package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.AssignmentRequest;
import com.amlakie.usermanagment.dto.CarReqRes;
import com.amlakie.usermanagment.dto.RentCarReqRes;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.RentCar;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import com.amlakie.usermanagment.repository.RentCarRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CarManagementService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentCarRepository rentCarRepository;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public CarReqRes registerCar(CarReqRes registrationRequest) {
        CarReqRes response = new CarReqRes();
        try {
            Car car = new Car();
            car.setPlateNumber(registrationRequest.getPlateNumber());
            car.setOwnerName(registrationRequest.getOwnerName());
            car.setOwnerPhone(registrationRequest.getOwnerPhone());
            car.setModel(registrationRequest.getModel());
            car.setCarType(registrationRequest.getCarType());
            car.setManufactureYear(Integer.parseInt(registrationRequest.getManufactureYear()));
            car.setMotorCapacity(registrationRequest.getMotorCapacity());
            car.setKmPerLiter(Float.parseFloat(registrationRequest.getKmPerLiter()));
            car.setTotalKm(registrationRequest.getTotalKm());
            car.setFuelType(registrationRequest.getFuelType());
            car.setStatus(registrationRequest.getStatus());
            car.setParkingLocation(registrationRequest.getParkingLocation());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            car.setCreatedBy(authentication.getName());

            Car savedCar = carRepository.save(car);
            response.setCar(savedCar);
            response.setMessage("Car Registered Successfully");
            response.setCodStatus(200);
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getAllCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findAll();
            response.setCarList(cars);
            response.setCodStatus(200);
            response.setMessage("All cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getCarById(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<Car> car = carRepository.findById(id);
            if (car.isPresent()) {
                response.setCar(car.get());
                response.setCodStatus(200);
                response.setMessage("Car retrieved successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes updateCar(Long id, CarReqRes updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<Car> carOptional = carRepository.findById(id);
            if (carOptional.isPresent()) {
                Car existingCar = carOptional.get();
                existingCar.setPlateNumber(updateRequest.getPlateNumber());
                existingCar.setOwnerName(updateRequest.getOwnerName());
                existingCar.setOwnerPhone(updateRequest.getOwnerPhone());
                existingCar.setModel(updateRequest.getModel());
                existingCar.setCarType(updateRequest.getCarType());
                existingCar.setManufactureYear(Integer.parseInt(updateRequest.getManufactureYear()));
                existingCar.setMotorCapacity(updateRequest.getMotorCapacity());
                existingCar.setKmPerLiter(Float.parseFloat(updateRequest.getKmPerLiter()));
                existingCar.setTotalKm(updateRequest.getTotalKm());
                existingCar.setFuelType(updateRequest.getFuelType());
                existingCar.setStatus(updateRequest.getStatus());
                existingCar.setParkingLocation(updateRequest.getParkingLocation());

                Car updatedCar = carRepository.save(existingCar);
                response.setCar(updatedCar);
                response.setCodStatus(200);
                response.setMessage("Car updated successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes deleteCar(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            if (carRepository.existsById(id)) {
                carRepository.deleteById(id);
                response.setCodStatus(200);
                response.setMessage("Car deleted successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes searchCars(String query) {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findByPlateNumberContainingOrOwnerNameContainingOrModelContaining(
                    query, query, query);
            response.setCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Search results retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes updateStatus(String plateNumber, CarReqRes updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<Car> carOptional = carRepository.findByPlateNumber(plateNumber);
            if (carOptional.isPresent()) {
                Car existingCar = carOptional.get();
                existingCar.setStatus(updateRequest.getStatus());

                Car updatedCar = carRepository.save(existingCar);
                response.setCar(updatedCar);
                response.setCodStatus(200);
                response.setMessage("Car status updated successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getApprovedCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<String> statuses = Arrays.asList("InspectedAndReady", "In_transfer");
            List<Car> cars = carRepository.findByStatusIn(statuses);
            response.setCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Approved and in-transfer cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getInTransferCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<String> statuses = Arrays.asList("In_transfer", "In_transfer");
            List<Car> cars = carRepository.findByStatusIn(statuses);
            response.setCarList(cars);
            response.setCodStatus(200);
            response.setMessage("In-transfer cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    @Transactional
    public CarReqRes createAssignment(AssignmentRequest request) {
        CarReqRes response = new CarReqRes();
        try {
            if (request.getRequestLetterNo() == null || request.getRequestLetterNo().isEmpty()) {
                throw new IllegalArgumentException("Request letter number is required");
            }
            if (request.getLicenseExpiryDate() == null || request.getLicenseExpiryDate().isEmpty()) {
                throw new IllegalArgumentException("License expiry date is required");
            }

            LocalDateTime requestDateTime = LocalDate.parse(request.getRequestDate()).atStartOfDay();

            AssignmentHistory history = new AssignmentHistory();
            history.setRequestLetterNo(request.getRequestLetterNo());
            history.setRequestDate(requestDateTime);
            history.setAssignedDate(requestDateTime);
            history.setRequesterName(request.getRequesterName());
            history.setRentalType(request.getRentalType());
            history.setPosition(request.getPosition());
            history.setDepartment(request.getDepartment());
            history.setPhoneNumber(request.getPhoneNumber());
            history.setTravelWorkPercentage(request.getTravelWorkPercentage());
            history.setShortNoticePercentage(request.getShortNoticePercentage());
            history.setMobilityIssue(request.getMobilityIssue());
            history.setGender(request.getGender());
            history.setTotalPercentage(request.getTotalPercentage());
            history.setStatus(request.getStatus());
            history.setLicenseExpiryDate(request.getLicenseExpiryDate());
            history.setDriverLicenseNumber(request.getDriverLicenseNumber());

            if (request.getDriverLicenseFile() != null && !request.getDriverLicenseFile().isEmpty()) {
                String filePath = storeFile(request.getDriverLicenseFile());
                history.setDriverLicenseFiles(List.of(filePath));
                history.setDriverLicenseFileTypes(List.of(request.getDriverLicenseFile().getContentType()));
                history.setDriverLicenseFileNames(List.of(request.getDriverLicenseFile().getOriginalFilename()));
            }

            if ("Level 1".equals(request.getPosition())) {
                history.setModel(request.getModel());
                history.setNumberOfCar(request.getNumberOfCar());
            } else {
                history.setNumberOfCar("1");
            }

            List<String> allPlateNumbers = new ArrayList<>();
            List<String> allCarModels = new ArrayList<>();

            try {
                if (request.getCarId() != null) {
                    Car car = carRepository.findById(request.getCarId())
                            .orElseThrow(() -> new RuntimeException("Regular car not found with ID: " + request.getCarId()));
                    history.setCar(car);
                    allPlateNumbers.add(car.getPlateNumber());
                    allCarModels.add(car.getModel());
                }

                if (request.getCarIds() != null && !request.getCarIds().isEmpty()) {
                    Set<Car> cars = new HashSet<>(carRepository.findAllById(request.getCarIds()));
                    if (cars.size() != request.getCarIds().size()) {
                        throw new RuntimeException("One or more regular cars not found");
                    }
                    history.setMultipleCars(cars);
                    cars.forEach(car -> {
                        allPlateNumbers.add(car.getPlateNumber());
                        allCarModels.add(car.getModel());
                    });
                }

                if (request.getRentCarId() != null) {
                    RentCar rentCar = rentCarRepository.findById(request.getRentCarId())
                            .orElseThrow(() -> new RuntimeException("Rent car not found with ID: " + request.getRentCarId()));
                    history.setRentCar(rentCar);
                    allPlateNumbers.add(rentCar.getPlateNumber());
                    allCarModels.add(rentCar.getModel());
                }

                if (request.getRentCarIds() != null && !request.getRentCarIds().isEmpty()) {
                    Set<RentCar> rentCars = new HashSet<>(rentCarRepository.findAllById(request.getRentCarIds()));
                    if (rentCars.size() != request.getRentCarIds().size()) {
                        throw new RuntimeException("One or more rent cars not found");
                    }
                    history.setMultipleRentCars(rentCars);
                    rentCars.forEach(rentCar -> {
                        allPlateNumbers.add(rentCar.getPlateNumber());
                        allCarModels.add(rentCar.getModel());
                    });
                }

                if (!allPlateNumbers.isEmpty()) {
                    history.setAllPlateNumbers(String.join(", ", allPlateNumbers));
                    history.setAllCarModels(String.join(", ", allCarModels));
                }

                assignmentHistoryRepository.save(history);

            } catch (Exception e) {
                if (history.getDriverLicenseFiles() != null && !history.getDriverLicenseFiles().isEmpty()) {
                    try {
                        Files.deleteIfExists(Paths.get(history.getDriverLicenseFiles().get(0)));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw e;
            }

            response.setCodStatus(200);
            response.setMessage("Assignment created successfully");
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("assignmentId", history.getId());
            responseData.put("totalVehiclesAssigned", allPlateNumbers.size());
            responseData.put("plateNumbers", history.getAllPlateNumbers());
            responseData.put("carModels", history.getAllCarModels());
            responseData.put("licenseExpiryDate", history.getLicenseExpiryDate());
            responseData.put("driverLicenseFiles", history.getDriverLicenseFiles());
            response.setResponseData(responseData);

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    private String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Invalid file type. Only images and PDFs are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = "license_" + UUID.randomUUID() + fileExtension;

        Path targetLocation = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }

    public byte[] getFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found");
        }
        return Files.readAllBytes(path);
    }

    public String getFileContentType(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String contentType = Files.probeContentType(path);
        return contentType != null ? contentType : "application/octet-stream";
    }

    @Transactional
    public CarReqRes uploadDriverLicense(Long id, MultipartFile file) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory assignment = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

            String filePath = storeFile(file);

            if (assignment.getDriverLicenseFiles() == null) {
                assignment.setDriverLicenseFiles(new ArrayList<>());
                assignment.setDriverLicenseFileTypes(new ArrayList<>());
                assignment.setDriverLicenseFileNames(new ArrayList<>());
            }

            assignment.getDriverLicenseFiles().add(filePath);
            assignment.getDriverLicenseFileTypes().add(file.getContentType());
            assignment.getDriverLicenseFileNames().add(file.getOriginalFilename());

            assignmentHistoryRepository.save(assignment);

            response.setCodStatus(200);
            response.setMessage("Driver license uploaded successfully");
            response.setAssignmentHistory(assignment);
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getExpiringLicenses() {
        CarReqRes response = new CarReqRes();
        try {
            LocalDate today = LocalDate.now();
            LocalDate warningDate = today.plusDays(5);

            List<AssignmentHistory> expiringSoon = assignmentHistoryRepository
                    .findByLicenseExpiryDateBetween(today.toString(), warningDate.toString());

            List<AssignmentHistory> expired = assignmentHistoryRepository
                    .findByLicenseExpiryDateBefore(today.toString());

            Map<String, Object> result = new HashMap<>();
            result.put("expiringSoon", expiringSoon);
            result.put("expired", expired);

            response.setCodStatus(200);
            response.setMessage("License expiry check completed");
            response.setResponseData(result);

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    @Transactional
    public CarReqRes updateAssignmentHistory(Long id, AssignmentRequest updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Assignment history not found"));

            history.setAssignedDate(LocalDateTime.now());
            history.setStatus(updateRequest.getStatus());

            if (updateRequest.getCarId() != null || updateRequest.getCarIds() != null) {
                history.setCar(null);
                history.setMultipleCars(new HashSet<>());
            }
            if (updateRequest.getRentCarId() != null || updateRequest.getRentCarIds() != null) {
                history.setRentCar(null);
                history.setMultipleRentCars(new HashSet<>());
            }

            List<String> allPlateNumbers = new ArrayList<>();
            List<String> allCarModels = new ArrayList<>();

            if (updateRequest.getCarId() != null) {
                Car car = carRepository.findById(updateRequest.getCarId())
                        .orElseThrow(() -> new RuntimeException("Regular car not found"));
                history.setCar(car);
                allPlateNumbers.add(car.getPlateNumber());
                allCarModels.add(car.getModel());
            }

            if (updateRequest.getCarIds() != null && !updateRequest.getCarIds().isEmpty()) {
                Set<Car> cars = new HashSet<>(carRepository.findAllById(updateRequest.getCarIds()));
                if (cars.size() != updateRequest.getCarIds().size()) {
                    throw new RuntimeException("One or more regular cars not found");
                }
                history.setMultipleCars(cars);
                cars.forEach(car -> {
                    allPlateNumbers.add(car.getPlateNumber());
                    allCarModels.add(car.getModel());
                });
            }

            if (updateRequest.getRentCarId() != null) {
                RentCar rentCar = rentCarRepository.findById(updateRequest.getRentCarId())
                        .orElseThrow(() -> new RuntimeException("Rent car not found"));
                history.setRentCar(rentCar);
                allPlateNumbers.add(rentCar.getPlateNumber());
                allCarModels.add(rentCar.getModel());
            }

            if (updateRequest.getRentCarIds() != null && !updateRequest.getRentCarIds().isEmpty()) {
                Set<RentCar> rentCars = new HashSet<>(rentCarRepository.findAllById(updateRequest.getRentCarIds()));
                if (rentCars.size() != updateRequest.getRentCarIds().size()) {
                    throw new RuntimeException("One or more rent cars not found");
                }
                history.setMultipleRentCars(rentCars);
                rentCars.forEach(rentCar -> {
                    allPlateNumbers.add(rentCar.getPlateNumber());
                    allCarModels.add(rentCar.getModel());
                });
            }

            if (!allPlateNumbers.isEmpty()) {
                history.setPlateNumber(String.join(", ", allPlateNumbers));
                history.setAllPlateNumbers(String.join(", ", allPlateNumbers));
                history.setAllCarModels(String.join(", ", allCarModels));

                if ("Level 1".equals(history.getPosition())) {
                    int totalCars = (history.getCar() != null ? 1 : 0) +
                            (history.getMultipleCars() != null ? history.getMultipleCars().size() : 0) +
                            (history.getRentCar() != null ? 1 : 0) +
                            (history.getMultipleRentCars() != null ? history.getMultipleRentCars().size() : 0);
                    history.setNumberOfCar(String.valueOf(totalCars));
                }
            }

            assignmentHistoryRepository.save(history);

            response.setCodStatus(200);
            response.setMessage("Assignment updated successfully");
            response.setAssignmentHistory(history);

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getAllAssignmentHistories() {
        CarReqRes response = new CarReqRes();
        try {
            List<AssignmentHistory> histories = assignmentHistoryRepository.findAll();
            response.setAssignmentHistoryList(histories);
            response.setCodStatus(200);
            response.setMessage("All assignment histories retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getPendingRequests() {
        CarReqRes response = new CarReqRes();
        try {
            List<String> statuses = Arrays.asList("Pending", "In_transfer");
            List<AssignmentHistory> histories = assignmentHistoryRepository.findByStatusIn(statuses);
            response.setAssignmentHistoryList(histories);
            response.setCodStatus(200);
            response.setMessage("Pending and in-transfer requests retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getPendingAndSemiPendingRequests() {
        CarReqRes response = new CarReqRes();
        try {
            List<String> statuses = Arrays.asList("Pending", "In_transfer", "SemiAssigned");
            List<AssignmentHistory> histories = assignmentHistoryRepository.findByStatusIn(statuses);
            response.setAssignmentHistoryList(histories);
            response.setCodStatus(200);
            response.setMessage("Pending and semi-pending requests retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getAssignmentHistoryById(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Assignment history not found"));
            response.setAssignmentHistory(history);
            response.setCodStatus(200);
            response.setMessage("Assignment history retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes deleteAssignmentHistory(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Assignment history not found"));

            if (history.getCar() != null) {
                Car car = history.getCar();
                car.setStatus("Approved");
                carRepository.save(car);
            }

            if (history.getDriverLicenseFiles() != null) {
                for (String filePath : history.getDriverLicenseFiles()) {
                    try {
                        Files.deleteIfExists(Paths.get(filePath));
                    } catch (IOException e) {
                        // Log warning but continue with deletion
                    }
                }
            }

            assignmentHistoryRepository.delete(history);

            response.setCodStatus(200);
            response.setMessage("Assignment history deleted successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes updateAssignmentStatus(Long id, AssignmentRequest updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory existingAssignment = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            existingAssignment.setStatus(updateRequest.getStatus());
            AssignmentHistory updatedAssignment = assignmentHistoryRepository.save(existingAssignment);

            response.setAssignmentHistory(updatedAssignment);
            response.setCodStatus(200);
            response.setMessage("Assignment status updated successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }
}