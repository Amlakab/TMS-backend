package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.*;
import com.amlakie.usermanagment.entity.BodyCondition;
import com.amlakie.usermanagment.entity.BodyProblem;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.CarInspection;
import com.amlakie.usermanagment.repository.CarInspectionRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarInspectionService {

    @Autowired
    private CarInspectionRepository inspectionRepository;

    @Autowired
    private CarRepository carRepository;

    public CarInspectionReqRes createInspection(CarInspectionReqRes request) {
        CarInspectionReqRes response = new CarInspectionReqRes();
        try {
            // Check if car exists, if not create it
            if (!carRepository.existsByPlateNumber(request.getPlateNumber())) {
                Car newCar = new Car();
                newCar.setPlateNumber(request.getPlateNumber());
                // Set default values or get from request if available
                newCar.setOwnerName("Unknown");
                newCar.setOwnerPhone("0000000000");
                // ... set other required fields ...
                carRepository.save(newCar);
            }

            CarInspection inspection = mapRequestToEntity(request);
            CarInspection savedInspection = inspectionRepository.save(inspection);

            response = mapEntityToResponse(savedInspection);
            response.setCodStatus(201);
            response.setMessage("Inspection created successfully");

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarInspectionListResponse getAllInspections() {
        CarInspectionListResponse response = new CarInspectionListResponse();
        try {
            List<CarInspection> inspections = inspectionRepository.findAll();
            response.setInspections(inspections.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList()));
            response.setCodStatus(200);
            response.setMessage("Inspections retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarInspectionReqRes getInspectionById(Long id) {
        CarInspectionReqRes response = new CarInspectionReqRes();
        try {
            CarInspection inspection = inspectionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inspection not found"));
            response = mapEntityToResponse(inspection);
            response.setCodStatus(200);
            response.setMessage("Inspection retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(404);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarInspectionReqRes updateInspection(Long id, CarInspectionReqRes request) {
        CarInspectionReqRes response = new CarInspectionReqRes();
        try {
            CarInspection existingInspection = inspectionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Inspection not found"));

            CarInspection updatedInspection = mapRequestToEntity(request);
            updatedInspection.setId(existingInspection.getId());

            CarInspection savedInspection = inspectionRepository.save(updatedInspection);
            response = mapEntityToResponse(savedInspection);
            response.setCodStatus(200);
            response.setMessage("Inspection updated successfully");

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarInspectionReqRes deleteInspection(Long id) {
        CarInspectionReqRes response = new CarInspectionReqRes();
        try {
            inspectionRepository.deleteById(id);
            response.setCodStatus(200);
            response.setMessage("Inspection deleted successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public CarInspectionListResponse getInspectionsByPlateNumber(String plateNumber) {
        CarInspectionListResponse response = new CarInspectionListResponse();
        try {
            List<CarInspection> inspections = inspectionRepository.findByPlateNumber(plateNumber);
            response.setInspections(inspections.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList()));
            response.setCodStatus(200);
            response.setMessage("Inspections retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    // Helper methods
    private CarInspection mapRequestToEntity(CarInspectionReqRes request) {
        CarInspection inspection = new CarInspection();
        inspection.setPlateNumber(request.getPlateNumber());

        // Map body condition
        BodyCondition bodyCondition = new BodyCondition();
        bodyCondition.setBodyCollision(mapBodyProblemDTO(request.getBodyCondition().getBodyCollision()));
        bodyCondition.setBodyScratches(mapBodyProblemDTO(request.getBodyCondition().getBodyScratches()));
        bodyCondition.setPaintCondition(mapBodyProblemDTO(request.getBodyCondition().getPaintCondition()));
        bodyCondition.setBreakages(mapBodyProblemDTO(request.getBodyCondition().getBreakages()));
        bodyCondition.setCracks(mapBodyProblemDTO(request.getBodyCondition().getCracks()));

        inspection.setBodyCondition(bodyCondition);

        // Map mechanical attributes
        inspection.setEngineCondition(request.isEngineCondition());
        inspection.setFullInsurance(request.isFullInsurance());
        inspection.setEnginePower(request.isEnginePower());
        inspection.setSuspension(request.isSuspension());
        inspection.setBrakes(request.isBrakes());
        inspection.setSteering(request.isSteering());
        inspection.setGearbox(request.isGearbox());
        inspection.setMileage(request.isMileage());
        inspection.setFuelGauge(request.isFuelGauge());
        inspection.setTempGauge(request.isTempGauge());
        inspection.setOilGauge(request.isOilGauge());

        // Map interior attributes
        inspection.setEngineExhaust(request.isEngineExhaust());
        inspection.setSeatComfort(request.isSeatComfort());
        inspection.setSeatFabric(request.isSeatFabric());
        inspection.setFloorMat(request.isFloorMat());
        inspection.setRearViewMirror(request.isRearViewMirror());
        inspection.setCarTab(request.isCarTab());
        inspection.setMirrorAdjustment(request.isMirrorAdjustment());
        inspection.setDoorLock(request.isDoorLock());
        inspection.setVentilationSystem(request.isVentilationSystem());
        inspection.setDashboardDecoration(request.isDashboardDecoration());
        inspection.setSeatBelt(request.isSeatBelt());
        inspection.setSunshade(request.isSunshade());
        inspection.setWindowCurtain(request.isWindowCurtain());
        inspection.setInteriorRoof(request.isInteriorRoof());
        inspection.setCarIgnition(request.isCarIgnition());
        inspection.setFuelConsumption(request.isFuelConsumption());
        inspection.setHeadlights(request.isHeadlights());
        inspection.setRainWiper(request.isRainWiper());
        inspection.setTurnSignalLight(request.isTurnSignalLight());
        inspection.setBrakeLight(request.isBrakeLight());
        inspection.setLicensePlateLight(request.isLicensePlateLight());
        inspection.setClock(request.isClock());
        inspection.setRpm(request.isRpm());
        inspection.setBatteryStatus(request.isBatteryStatus());
        inspection.setChargingIndicator(request.isChargingIndicator());

        return inspection;
    }

    private BodyProblem mapBodyProblemDTO(BodyProblemDTO dto) {
        BodyProblem problem = new BodyProblem();
        problem.setProblem(dto.isProblem());
        problem.setSeverity(dto.getSeverity());
        problem.setNotes(dto.getNotes());
        return problem;
    }

    private CarInspectionReqRes mapEntityToResponse(CarInspection inspection) {
        CarInspectionReqRes response = new CarInspectionReqRes();
        response.setId(inspection.getId());
        response.setPlateNumber(inspection.getPlateNumber());
        response.setInspectionDate(inspection.getInspectionDate());

        // Map body condition
        BodyConditionDTO bodyConditionDTO = new BodyConditionDTO();
        bodyConditionDTO.setBodyCollision(mapBodyProblemEntity(inspection.getBodyCondition().getBodyCollision()));
        bodyConditionDTO.setBodyScratches(mapBodyProblemEntity(inspection.getBodyCondition().getBodyScratches()));
        bodyConditionDTO.setPaintCondition(mapBodyProblemEntity(inspection.getBodyCondition().getPaintCondition()));
        bodyConditionDTO.setBreakages(mapBodyProblemEntity(inspection.getBodyCondition().getBreakages()));
        bodyConditionDTO.setCracks(mapBodyProblemEntity(inspection.getBodyCondition().getCracks()));

        response.setBodyCondition(bodyConditionDTO);

        // Map mechanical attributes
        response.setEngineCondition(inspection.isEngineCondition());
        response.setFullInsurance(inspection.isFullInsurance());
        response.setEnginePower(inspection.isEnginePower());
        response.setSuspension(inspection.isSuspension());
        response.setBrakes(inspection.isBrakes());
        response.setSteering(inspection.isSteering());
        response.setGearbox(inspection.isGearbox());
        response.setMileage(inspection.isMileage());
        response.setFuelGauge(inspection.isFuelGauge());
        response.setTempGauge(inspection.isTempGauge());
        response.setOilGauge(inspection.isOilGauge());

        // Map interior attributes
        response.setEngineExhaust(inspection.isEngineExhaust());
        response.setSeatComfort(inspection.isSeatComfort());
        response.setSeatFabric(inspection.isSeatFabric());
        response.setFloorMat(inspection.isFloorMat());
        response.setRearViewMirror(inspection.isRearViewMirror());
        response.setCarTab(inspection.isCarTab());
        response.setMirrorAdjustment(inspection.isMirrorAdjustment());
        response.setDoorLock(inspection.isDoorLock());
        response.setVentilationSystem(inspection.isVentilationSystem());
        response.setDashboardDecoration(inspection.isDashboardDecoration());
        response.setSeatBelt(inspection.isSeatBelt());
        response.setSunshade(inspection.isSunshade());
        response.setWindowCurtain(inspection.isWindowCurtain());
        response.setInteriorRoof(inspection.isInteriorRoof());
        response.setCarIgnition(inspection.isCarIgnition());
        response.setFuelConsumption(inspection.isFuelConsumption());
        response.setHeadlights(inspection.isHeadlights());
        response.setRainWiper(inspection.isRainWiper());
        response.setTurnSignalLight(inspection.isTurnSignalLight());
        response.setBrakeLight(inspection.isBrakeLight());
        response.setLicensePlateLight(inspection.isLicensePlateLight());
        response.setClock(inspection.isClock());
        response.setRpm(inspection.isRpm());
        response.setBatteryStatus(inspection.isBatteryStatus());
        response.setChargingIndicator(inspection.isChargingIndicator());

        return response;
    }

    private BodyProblemDTO mapBodyProblemEntity(BodyProblem problem) {
        BodyProblemDTO dto = new BodyProblemDTO();
        dto.setProblem(problem.isProblem());
        dto.setSeverity(problem.getSeverity());
        dto.setNotes(problem.getNotes());
        return dto;
    }
}