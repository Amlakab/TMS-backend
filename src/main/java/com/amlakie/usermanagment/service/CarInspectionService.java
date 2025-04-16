package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.*;
import com.amlakie.usermanagment.entity.*;
import com.amlakie.usermanagment.repository.CarInspectionRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarInspectionService {

    @Autowired
    private CarInspectionRepository inspectionRepository;

    @Autowired
    private CarRepository carRepository;

    public CarInspectionReqRes createInspection(CarInspectionReqRes request) {
        // Error checking before saving the inspection
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car inspection request is null");
        }

        // Check if car exists, if not create it
        Car car = carRepository.findByPlateNumber(request.getPlateNumber())
                .orElseGet(() -> {
                    Car newCar = new Car();
                    newCar.setPlateNumber(request.getPlateNumber());
                    newCar.setOwnerName("Unknown"); // Set default value
                    newCar.setCarType("Unknown"); // Set default values
                    newCar.setOwnerPhone("0000000000");
                    newCar.setModel("UNKNOWN");
                    newCar.setFuelType("UNKNOWN");
                    newCar.setCreatedBy("SYSTEM_AUTO_CREATE");
                    newCar.setFuelType("UNKNOWN");
                    newCar.setParkingLocation("UNKNOWN");
                    newCar.setMotorCapacity("UNKNOWN");
                    newCar.setTotalKm("UNKNOWN");
                    newCar.setRegisteredDate(null);
                    newCar.setStatus("UNKNOWN");
                    newCar.setManufactureYear(0);
                    newCar.setKmPerLiter(0.0f);
                     newCar.setManufactureYear(0); // Or another appropriate default
                    return carRepository.save(newCar);
                });

        CarInspection inspection = mapRequestToEntity(request);
        inspection.setCar(car); // Add relationship to car

        CarInspection savedInspection;
        try {
            savedInspection = inspectionRepository.save(inspection);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving car inspection", e);
        }

        CarInspectionReqRes response = mapEntityToResponse(savedInspection);
        response.setCodStatus(201);
        response.setMessage("Inspection created successfully");
        return response;
    }

    public CarInspectionListResponse getAllInspections() {
        List<CarInspection> inspections;
        try {
            inspections = inspectionRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving all car inspections", e);
        }

        List<CarInspectionReqRes> inspectionDTOs = inspections.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        CarInspectionListResponse response = new CarInspectionListResponse();
        response.setInspections(inspectionDTOs);
        response.setCodStatus(200);
        response.setMessage("Inspections retrieved successfully");
        return response;
    }

    public CarInspectionReqRes getInspectionById(Long id) {
        Optional<CarInspection> inspectionOptional;
        try {
            inspectionOptional = inspectionRepository.findById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving car inspection with id: " + id, e);
        }

        CarInspection inspection = inspectionOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found"));

        CarInspectionReqRes response = mapEntityToResponse(inspection);
        response.setCodStatus(200);
        response.setMessage("Inspection retrieved successfully");
        return response;
    }

    public CarInspectionReqRes updateInspection(Long id, CarInspectionReqRes request) {
        Optional<CarInspection> existingInspectionOptional;
        try {
            existingInspectionOptional = inspectionRepository.findById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding car inspection with id: " + id, e);
        }

        CarInspection existingInspection = existingInspectionOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found"));

        CarInspection updatedInspection = mapRequestToEntity(request);
        updatedInspection.setId(existingInspection.getId());

        CarInspection savedInspection;
        try {
            savedInspection = inspectionRepository.save(updatedInspection);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating car inspection with id: " + id, e);
        }

        CarInspectionReqRes response = mapEntityToResponse(savedInspection);
        response.setCodStatus(200);
        response.setMessage("Inspection updated successfully");
        return response;
    }

    public CarInspectionReqRes deleteInspection(Long id) {
        try {
            inspectionRepository.deleteById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting car inspection with id: " + id, e);
        }

        CarInspectionReqRes response = new CarInspectionReqRes();
        response.setCodStatus(200);
        response.setMessage("Inspection deleted successfully");
        return response;
    }

    public CarInspectionListResponse getInspectionsByPlateNumber(String plateNumber) {
        List<CarInspection> inspections;
        try {
            inspections = inspectionRepository.findByPlateNumber(plateNumber);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving car inspections by plate number: " + plateNumber, e);
        }

        List<CarInspectionReqRes> inspectionDTOs = inspections.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        CarInspectionListResponse response = new CarInspectionListResponse();
        response.setInspections(inspectionDTOs);
        response.setCodStatus(200);
        response.setMessage("Inspections retrieved successfully");
        return response;
    }

    // Helper methods
    private CarInspection mapRequestToEntity(CarInspectionReqRes request) {
        CarInspection inspection = new CarInspection();
        inspection.setPlateNumber(request.getPlateNumber());
        inspection.setInspectionDate(request.getInspectionDate());
        inspection.setInspectorName(request.getInspectorName());
        inspection.setInspectionStatus(request.getInspectionStatus().toString());
        inspection.setServiceStatus(request.getServiceStatus().toString());
        inspection.setBodyScore(request.getBodyScore());
        inspection.setInteriorScore(request.getInteriorScore());
        inspection.setNotes(request.getNotes());
        // Map Mechanical
        inspection.setMechanical(mapMechanicalDTOtoEntity(request.getMechanical()));

        // Map Body
        inspection.setBody(mapBodyDTOtoEntity(request.getBody()));

        // Map Interior
        inspection.setInterior(mapInteriorDTOtoEntity(request.getInterior()));
        return inspection;
    }

    private MechanicalInspection mapMechanicalDTOtoEntity(MechanicalInspectionDTO mechanicalDTO) {
        MechanicalInspection mechanical = new MechanicalInspection();
        mechanical.setEngineCondition(mechanicalDTO.isEngineCondition());
        mechanical.setEnginePower(mechanicalDTO.isEnginePower());
        mechanical.setSuspension(mechanicalDTO.isSuspension());
        mechanical.setBrakes(mechanicalDTO.isBrakes());
        mechanical.setSteering(mechanicalDTO.isSteering());
        mechanical.setGearbox(mechanicalDTO.isGearbox());
        mechanical.setMileage(mechanicalDTO.isMileage());
        mechanical.setFuelGauge(mechanicalDTO.isFuelGauge());
        mechanical.setTempGauge(mechanicalDTO.isTempGauge());
        mechanical.setOilGauge(mechanicalDTO.isOilGauge());
        return mechanical;
    }

    private BodyInspection mapBodyDTOtoEntity(BodyInspectionDTO bodyDTO) {
        BodyInspection body = new BodyInspection();
        body.setBodyCollision(mapItemConditionDTO(bodyDTO.getBodyCollision()));
        body.setBodyScratches(mapItemConditionDTO(bodyDTO.getBodyScratches()));
        body.setPaintCondition(mapItemConditionDTO(bodyDTO.getPaintCondition()));
        body.setBreakages(mapItemConditionDTO(bodyDTO.getBreakages()));
        body.setCracks(mapItemConditionDTO(bodyDTO.getCracks()));
        return body;
    }

    private InteriorInspection mapInteriorDTOtoEntity(InteriorInspectionDTO interiorDTO) {
        InteriorInspection interior = new InteriorInspection();
        interior.setEngineExhaust(mapItemConditionDTO(interiorDTO.getEngineExhaust()));
        interior.setSeatComfort(mapItemConditionDTO(interiorDTO.getSeatComfort()));
        interior.setSeatFabric(mapItemConditionDTO(interiorDTO.getSeatFabric()));
        interior.setFloorMat(mapItemConditionDTO(interiorDTO.getFloorMat()));
        interior.setRearViewMirror(mapItemConditionDTO(interiorDTO.getRearViewMirror()));
        interior.setCarTab(mapItemConditionDTO(interiorDTO.getCarTab()));
        interior.setMirrorAdjustment(mapItemConditionDTO(interiorDTO.getMirrorAdjustment()));
        interior.setDoorLock(mapItemConditionDTO(interiorDTO.getDoorLock()));
        interior.setVentilationSystem(mapItemConditionDTO(interiorDTO.getVentilationSystem()));
        interior.setDashboardDecoration(mapItemConditionDTO(interiorDTO.getDashboardDecoration()));
        interior.setSeatBelt(mapItemConditionDTO(interiorDTO.getSeatBelt()));
        interior.setSunshade(mapItemConditionDTO(interiorDTO.getSunshade()));
        interior.setWindowCurtain(mapItemConditionDTO(interiorDTO.getWindowCurtain()));
        interior.setInteriorRoof(mapItemConditionDTO(interiorDTO.getInteriorRoof()));
        interior.setCarIgnition(mapItemConditionDTO(interiorDTO.getCarIgnition()));
        interior.setFuelConsumption(mapItemConditionDTO(interiorDTO.getFuelConsumption()));
        interior.setHeadlights(mapItemConditionDTO(interiorDTO.getHeadlights()));
        interior.setRainWiper(mapItemConditionDTO(interiorDTO.getRainWiper()));
        interior.setTurnSignalLight(mapItemConditionDTO(interiorDTO.getTurnSignalLight()));
        interior.setBrakeLight(mapItemConditionDTO(interiorDTO.getBrakeLight()));
        interior.setLicensePlateLight(mapItemConditionDTO(interiorDTO.getLicensePlateLight()));
        interior.setClock(mapItemConditionDTO(interiorDTO.getClock()));
        interior.setRpm(mapItemConditionDTO(interiorDTO.getRpm()));
        interior.setBatteryStatus(mapItemConditionDTO(interiorDTO.getBatteryStatus()));
        interior.setChargingIndicator(mapItemConditionDTO(interiorDTO.getChargingIndicator()));
        return interior;
    }

    private ItemCondition mapItemConditionDTO(ItemConditionDTO itemConditionDTO) {
        ItemCondition itemCondition = new ItemCondition();
        itemCondition.setProblem(itemConditionDTO.getProblem());
        itemCondition.setSeverity(itemConditionDTO.getSeverity().toString());
        itemCondition.setNotes(itemConditionDTO.getNotes());
        return itemCondition;
    }

    private CarInspectionReqRes mapEntityToResponse(CarInspection inspection) {
        CarInspectionReqRes response = new CarInspectionReqRes();
        response.setId(inspection.getId());
        response.setPlateNumber(inspection.getPlateNumber());
        response.setInspectionDate(inspection.getInspectionDate());
        response.setInspectorName(inspection.getInspectorName());
        response.setInspectionStatus(CarInspectionReqRes.InspectionStatus.valueOf(inspection.getInspectionStatus()));
        response.setServiceStatus(CarInspectionReqRes.ServiceStatus.valueOf(inspection.getServiceStatus()));
        response.setBodyScore(inspection.getBodyScore());
        response.setInteriorScore(inspection.getInteriorScore());
        response.setNotes(inspection.getNotes());

        // Map Mechanical
        response.setMechanical(mapMechanicalEntityToDTO(inspection.getMechanical()));

        // Map Body
        response.setBody(mapBodyEntityToDTO(inspection.getBody()));

        // Map Interior
        response.setInterior(mapInteriorEntityToDTO(inspection.getInterior()));
        return response;
    }

    private MechanicalInspectionDTO mapMechanicalEntityToDTO(MechanicalInspection mechanical) {
        MechanicalInspectionDTO mechanicalDTO = new MechanicalInspectionDTO();
        mechanicalDTO.setEngineCondition(mechanical.isEngineCondition());
        mechanicalDTO.setEnginePower(mechanical.isEnginePower());
        mechanicalDTO.setSuspension(mechanical.isSuspension());
        mechanicalDTO.setBrakes(mechanical.isBrakes());
        mechanicalDTO.setSteering(mechanical.isSteering());
        mechanicalDTO.setGearbox(mechanical.isGearbox());
        mechanicalDTO.setMileage(mechanical.isMileage());
        mechanicalDTO.setFuelGauge(mechanical.isFuelGauge());
        mechanicalDTO.setTempGauge(mechanical.isTempGauge());
        mechanicalDTO.setOilGauge(mechanical.isOilGauge());
        return mechanicalDTO;
    }

    private BodyInspectionDTO mapBodyEntityToDTO(BodyInspection body) {
        BodyInspectionDTO bodyDTO = new BodyInspectionDTO();
        bodyDTO.setBodyCollision(mapItemConditionEntity(body.getBodyCollision()));
        bodyDTO.setBodyScratches(mapItemConditionEntity(body.getBodyScratches()));
        bodyDTO.setPaintCondition(mapItemConditionEntity(body.getPaintCondition()));
        bodyDTO.setBreakages(mapItemConditionEntity(body.getBreakages()));
        bodyDTO.setCracks(mapItemConditionEntity(body.getCracks()));
        return bodyDTO;
    }

    private InteriorInspectionDTO mapInteriorEntityToDTO(InteriorInspection interior) {
        InteriorInspectionDTO interiorDTO = new InteriorInspectionDTO();
        interiorDTO.setEngineExhaust(mapItemConditionEntity(interior.getEngineExhaust()));
        interiorDTO.setSeatComfort(mapItemConditionEntity(interior.getSeatComfort()));
        interiorDTO.setSeatFabric(mapItemConditionEntity(interior.getSeatFabric()));
        interiorDTO.setFloorMat(mapItemConditionEntity(interior.getFloorMat()));
        interiorDTO.setRearViewMirror(mapItemConditionEntity(interior.getRearViewMirror()));
        interiorDTO.setCarTab(mapItemConditionEntity(interior.getCarTab()));
        interiorDTO.setMirrorAdjustment(mapItemConditionEntity(interior.getMirrorAdjustment()));
        interiorDTO.setDoorLock(mapItemConditionEntity(interior.getDoorLock()));
        interiorDTO.setVentilationSystem(mapItemConditionEntity(interior.getVentilationSystem()));
        interiorDTO.setDashboardDecoration(mapItemConditionEntity(interior.getDashboardDecoration()));
        interiorDTO.setSeatBelt(mapItemConditionEntity(interior.getSeatBelt()));
        interiorDTO.setSunshade(mapItemConditionEntity(interior.getSunshade()));
        interiorDTO.setWindowCurtain(mapItemConditionEntity(interior.getWindowCurtain()));
        interiorDTO.setInteriorRoof(mapItemConditionEntity(interior.getInteriorRoof()));
        interiorDTO.setCarIgnition(mapItemConditionEntity(interior.getCarIgnition()));
        interiorDTO.setFuelConsumption(mapItemConditionEntity(interior.getFuelConsumption()));
        interiorDTO.setHeadlights(mapItemConditionEntity(interior.getHeadlights()));
        interiorDTO.setRainWiper(mapItemConditionEntity(interior.getRainWiper()));
        interiorDTO.setTurnSignalLight(mapItemConditionEntity(interior.getTurnSignalLight()));
        interiorDTO.setBrakeLight(mapItemConditionEntity(interior.getBrakeLight()));
        interiorDTO.setLicensePlateLight(mapItemConditionEntity(interior.getLicensePlateLight()));
        interiorDTO.setClock(mapItemConditionEntity(interior.getClock()));
        interiorDTO.setRpm(mapItemConditionEntity(interior.getRpm()));
        interiorDTO.setBatteryStatus(mapItemConditionEntity(interior.getBatteryStatus()));
        interiorDTO.setChargingIndicator(mapItemConditionEntity(interior.getChargingIndicator()));
        return interiorDTO;
    }

    private ItemConditionDTO mapItemConditionEntity(ItemCondition itemCondition) {
        ItemConditionDTO itemConditionDTO = new ItemConditionDTO();
        itemConditionDTO.setProblem(itemCondition.isProblem());
        itemConditionDTO.setSeverity(ItemConditionDTO.Severity.valueOf(itemCondition.getSeverity()));
        itemConditionDTO.setNotes(itemCondition.getNotes());
        return itemConditionDTO;
    }
}