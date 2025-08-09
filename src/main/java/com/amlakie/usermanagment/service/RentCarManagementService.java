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
public class RentCarManagementService {

    @Autowired
    private RentCarRepository rentCarRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private String getValueOrDefault(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : "No";
    }

    public RentCarReqRes registerRentCar(RentCarReqRes registrationRequest) {
        RentCarReqRes response = new RentCarReqRes();
        try {
            RentCar rentCar = new RentCar();
            // Set all fields from registrationRequest to rentCar
            rentCar.setFrameNo(registrationRequest.getFrameNo());
            rentCar.setCompanyName(registrationRequest.getCompanyName());
            rentCar.setVehiclesUsed(registrationRequest.getVehiclesUsed());
            rentCar.setBodyType(registrationRequest.getBodyType());
            rentCar.setModel(registrationRequest.getModel());
            rentCar.setMotorNumber(registrationRequest.getMotorNumber());
            rentCar.setProYear(registrationRequest.getProYear());
            rentCar.setCc(registrationRequest.getCc());
            rentCar.setDepartment(registrationRequest.getDepartment());
            rentCar.setVehiclesType(registrationRequest.getVehiclesType());
            rentCar.setPlateNumber(registrationRequest.getPlateNumber());
            rentCar.setColor(registrationRequest.getColor());
            rentCar.setDoor(registrationRequest.getDoor());
            rentCar.setCylinder(registrationRequest.getCylinder());
            rentCar.setFuelType(registrationRequest.getFuelType());
            rentCar.setStatus(registrationRequest.getVehiclesStatus());
            rentCar.setOtherDescription(registrationRequest.getOtherDescription());
            rentCar.setDriverName(registrationRequest.getDriverName());
            rentCar.setDriverPhone(registrationRequest.getDriverPhone());
            rentCar.setDriverAddress(registrationRequest.getDriverAddress());
            rentCar.setDriverExperience(registrationRequest.getDriverExperience());
            rentCar.setNumberOfSeats(Integer.valueOf(registrationRequest.getNumberOfSeats()));

            // Use helper method for fields that should default to "No"
            rentCar.setRadio(getValueOrDefault(registrationRequest.getRadio()));
            rentCar.setAntena(getValueOrDefault(registrationRequest.getAntena()));
            rentCar.setKrik(getValueOrDefault(registrationRequest.getKrik()));
            rentCar.setKrikManesha(getValueOrDefault(registrationRequest.getKrikManesha()));
            rentCar.setTyerStatus(registrationRequest.getTyerStatus());
            rentCar.setGomaMaficha(getValueOrDefault(registrationRequest.getGomaMaficha()));
            rentCar.setMefcha(getValueOrDefault(registrationRequest.getMefcha()));
            rentCar.setReserveTayer(getValueOrDefault(registrationRequest.getReserveTayer()));
            rentCar.setGomaGet(getValueOrDefault(registrationRequest.getGomaGet()));
            rentCar.setPinsa(getValueOrDefault(registrationRequest.getPinsa()));
            rentCar.setKacavite(getValueOrDefault(registrationRequest.getKacavite()));
            rentCar.setFireProtection(getValueOrDefault(registrationRequest.getFireProtection()));

            rentCar.setSource(registrationRequest.getSource());
            rentCar.setVehiclesDonorName(registrationRequest.getVehiclesDonorName());
            rentCar.setDateOfIn(registrationRequest.getDateOfIn());
            rentCar.setDateOfOut(registrationRequest.getDateOfOut()); // This can be null
            rentCar.setVehiclesPhoto(registrationRequest.getVehiclesPhoto());
            rentCar.setVehiclesUserName(registrationRequest.getVehiclesUserName());
            rentCar.setPosition(registrationRequest.getPosition());
            rentCar.setLibre(registrationRequest.getLibre());
            rentCar.setTransmission(registrationRequest.getTransmission());
            rentCar.setKm(registrationRequest.getKm());
            rentCar.setStatus("InspectedAndReady");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            rentCar.setCreatedBy(authentication.getName());

            RentCar savedCar = rentCarRepository.save(rentCar);
            response.setRentCar(savedCar);
            response.setMessage("Rent Car Registered Successfully");
            response.setCodStatus(200);
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }
    // In RentCarManagementService.java
    public RentCarReqRes getBusAndMinibusRentCars() {
        RentCarReqRes response = new RentCarReqRes();
        List<String> typesToFind = Arrays.asList("bus", "mini bus", "minibus");

        try {
            List<RentCar> cars = rentCarRepository.findByBodyTypeInIgnoreCase(typesToFind);
            response.setRentCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Bus and minibus rent cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response; // Return the object you built
    }
    public RentCarReqRes getAllRentCars() {
        RentCarReqRes response = new RentCarReqRes();
        try {
            List<RentCar> cars = rentCarRepository.findAll();
            response.setRentCarList(cars);
            response.setCodStatus(200);
            response.setMessage("All rent cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public RentCarReqRes getRentCarById(Long id) {
        RentCarReqRes response = new RentCarReqRes();
        try {
            Optional<RentCar> car = rentCarRepository.findById(id);
            if (car.isPresent()) {
                response.setRentCar(car.get());
                response.setCodStatus(200);
                response.setMessage("Rent car retrieved successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Rent car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public RentCarReqRes updateRentCar(Long id, RentCarReqRes updateRequest) {
        RentCarReqRes response = new RentCarReqRes();
        try {
            Optional<RentCar> carOptional = rentCarRepository.findById(id);
            if (carOptional.isPresent()) {
                RentCar existingCar = carOptional.get();
                // Update all fields from updateRequest to existingCar
                existingCar.setFrameNo(updateRequest.getFrameNo());
                existingCar.setCompanyName(updateRequest.getCompanyName());
                existingCar.setVehiclesUsed(updateRequest.getVehiclesUsed());
                existingCar.setBodyType(updateRequest.getBodyType());
                existingCar.setModel(updateRequest.getModel());
                existingCar.setMotorNumber(updateRequest.getMotorNumber());
                existingCar.setProYear(updateRequest.getProYear());
                existingCar.setCc(updateRequest.getCc());
                existingCar.setDepartment(updateRequest.getDepartment());
                existingCar.setVehiclesType(updateRequest.getVehiclesType());
                existingCar.setPlateNumber(updateRequest.getPlateNumber());
                existingCar.setColor(updateRequest.getColor());
                existingCar.setDoor(updateRequest.getDoor());
                existingCar.setCylinder(updateRequest.getCylinder());
                existingCar.setFuelType(updateRequest.getFuelType());
                existingCar.setStatus(updateRequest.getVehiclesStatus());
                existingCar.setOtherDescription(updateRequest.getOtherDescription());
                if (updateRequest.getDriverName() != null) existingCar.setDriverName(updateRequest.getDriverName());
                if (updateRequest.getDriverPhone() != null) existingCar.setDriverPhone(updateRequest.getDriverPhone());
                if (updateRequest.getDriverAddress() != null) existingCar.setDriverAddress(updateRequest.getDriverAddress());
                if (updateRequest.getDriverExperience() != null) existingCar.setDriverExperience(updateRequest.getDriverExperience());
                if (updateRequest.getNumberOfSeats() != null) existingCar.setNumberOfSeats(Integer.valueOf(updateRequest.getNumberOfSeats()));

                existingCar.setRadio(getValueOrDefault(updateRequest.getRadio()));
                existingCar.setAntena(getValueOrDefault(updateRequest.getAntena()));
                existingCar.setKrik(getValueOrDefault(updateRequest.getKrik()));
                existingCar.setKrikManesha(getValueOrDefault(updateRequest.getKrikManesha()));
                existingCar.setTyerStatus(updateRequest.getTyerStatus());
                existingCar.setGomaMaficha(getValueOrDefault(updateRequest.getGomaMaficha()));
                existingCar.setMefcha(getValueOrDefault(updateRequest.getMefcha()));
                existingCar.setReserveTayer(getValueOrDefault(updateRequest.getReserveTayer()));
                existingCar.setGomaGet(getValueOrDefault(updateRequest.getGomaGet()));
                existingCar.setPinsa(getValueOrDefault(updateRequest.getPinsa()));
                existingCar.setKacavite(getValueOrDefault(updateRequest.getKacavite()));
                existingCar.setFireProtection(getValueOrDefault(updateRequest.getFireProtection()));

                existingCar.setSource(updateRequest.getSource());
                existingCar.setVehiclesDonorName(updateRequest.getVehiclesDonorName());
                existingCar.setDateOfIn(updateRequest.getDateOfIn());
                existingCar.setDateOfOut(updateRequest.getDateOfOut()); // This can be null
                existingCar.setVehiclesPhoto(updateRequest.getVehiclesPhoto());
                existingCar.setVehiclesUserName(updateRequest.getVehiclesUserName());
                existingCar.setPosition(updateRequest.getPosition());
                existingCar.setLibre(updateRequest.getLibre());
                existingCar.setTransmission(updateRequest.getTransmission());
                existingCar.setKm(updateRequest.getKm());

                RentCar updatedCar = rentCarRepository.save(existingCar);
                response.setRentCar(updatedCar);
                response.setCodStatus(200);
                response.setMessage("Rent car updated successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Rent car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public RentCarReqRes deleteRentCar(Long id) {
        RentCarReqRes response = new RentCarReqRes();
        try {
            if (rentCarRepository.existsById(id)) {
                rentCarRepository.deleteById(id);
                response.setCodStatus(200);
                response.setMessage("Rent car deleted successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Rent car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public RentCarReqRes searchRentCars(String query) {
        RentCarReqRes response = new RentCarReqRes();
        try {
            List<RentCar> cars = rentCarRepository
                    .findByPlateNumberContainingOrCompanyNameContainingOrModelContainingOrVehiclesUserNameContaining(
                            query, query, query, query);
            response.setRentCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Search results retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    @Transactional
    public RentCarReqRes updateAssignmentHistory(Long id, AssignmentRequest updateRequest) {
        RentCarReqRes response = new RentCarReqRes();
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

    public RentCarReqRes updateStatus(String plateNumber, RentCarReqRes updateRequest) {
        RentCarReqRes response = new RentCarReqRes();
        try {
            Optional<RentCar> carOptional = rentCarRepository.findByPlateNumber(plateNumber);
            if (carOptional.isPresent()) {
                RentCar existingCar = carOptional.get();
                existingCar.setStatus(updateRequest.getStatus());

                RentCar updatedCar = rentCarRepository.save(existingCar);
                response.setRentCar(updatedCar);
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

    public RentCarReqRes getApprovedCars() {
        RentCarReqRes response = new RentCarReqRes();
        try {
            List<RentCar> cars = rentCarRepository.findByStatus("InspectedAndReady");
            response.setRentCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Approved cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public RentCarReqRes getInTransferCars() {
        RentCarReqRes response = new RentCarReqRes();
        try {
            List<RentCar> cars = rentCarRepository.findByStatus("In_transfer");
            response.setRentCarList(cars);
            response.setCodStatus(200);
            response.setMessage("In-Transfer cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }





    @Transactional
    public RentCarReqRes createAssignment(AssignmentRequest request) {
        RentCarReqRes response = new RentCarReqRes();
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
}