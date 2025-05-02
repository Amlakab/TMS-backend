package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.AssignmentRequest;
import com.amlakie.usermanagment.dto.CarReqRes;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.RentCar;
import com.amlakie.usermanagment.entity.TravelRequest;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import com.amlakie.usermanagment.repository.RentCarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarManagementService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    RentCarRepository rentCarRepository;

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
                response.setMessage("Car deleteddddd successfully");
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

    // Add these methods to CarManagementService
    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;

    public CarReqRes getApprovedCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findByStatus("Approved");
            response.setCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Approved cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes createAssignment(AssignmentRequest request) {
        CarReqRes response = new CarReqRes();
        try {
            // Convert string date to LocalDateTime
            LocalDateTime requestDateTime = LocalDate.parse(request.getRequestDate())
                    .atStartOfDay();

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
            history.setPlateNumber(request.getPlateNumber());
            history.setStatus(request.getStatus());

            // Handle car assignment
            if (request.getCarId() != null) {
                Car car = carRepository.findById(request.getCarId())
                        .orElseThrow(() -> new RuntimeException("Car not found"));
                history.setCar(car);
            }

            if (request.getRentCarId() != null) {
                RentCar rentCar = rentCarRepository
                        .findById(request.getRentCarId())
                        .orElseThrow(() -> new RuntimeException("Rent car not found"));
                history.setCars(rentCar);
            }

            assignmentHistoryRepository.save(history);

            response.setCodStatus(200);
            response.setMessage("Assignment created successfully");
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

    public CarReqRes getPendingRequest() {
        CarReqRes response = new CarReqRes();
        try {
            List<AssignmentHistory> histories = assignmentHistoryRepository.findByStatus("Pending");
            response.setAssignmentHistoryList(histories);
            response.setCodStatus(200);
            response.setMessage("Pending histories retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes getAssignmentHistoryById(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<AssignmentHistory> history = assignmentHistoryRepository.findById(id);
            if (history.isPresent()) {
                response.setAssignmentHistory(history.get());
                response.setCodStatus(200);
                response.setMessage("Assignment history retrieved successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Assignment history not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarReqRes updateAssignmentHistory(Long id, AssignmentRequest updateRequest) {
        CarReqRes response = new CarReqRes();

        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Assignment history not found"));

            // Update history fields
            history.setAssignedDate( LocalDateTime.now());
            history.setRentalType(updateRequest.getRentalType());
            history.setPlateNumber(updateRequest.getPlateNumber());
            history.setStatus(updateRequest.getStatus());


            if (updateRequest.getCarId() != null) {
                Car car = carRepository.findById(updateRequest.getCarId())
                        .orElseThrow(() -> new RuntimeException("Car not found"));
                history.setCar(car);
            }

            if (updateRequest.getRentCarId() != null) {
                RentCar rentCar = rentCarRepository
                        .findById(updateRequest.getRentCarId())
                        .orElseThrow(() -> new RuntimeException("Rent car not found"));
                history.setCars(rentCar);
            }

            assignmentHistoryRepository.save(history);

            response.setCodStatus(200);
            response.setMessage("Assignment created successfully");
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

            // Release the associated car
            Car car = history.getCar();
            car.setStatus("Approved");
            carRepository.save(car);

            // Delete the history
            assignmentHistoryRepository.delete(history);

            response.setCodStatus(200);
            response.setMessage("Assignment history deleted successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }


}
