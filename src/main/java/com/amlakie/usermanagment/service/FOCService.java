package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.focform.FOCRequest;
import com.amlakie.usermanagment.entity.focform.FOC;
import com.amlakie.usermanagment.entity.focform.OilUsed;
import com.amlakie.usermanagment.repository.FOCRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FOCService {

    private final FOCRepository focRepository;

    @Transactional
    public FOC saveFOCForm(FOCRequest request) {
        FOC foc = new FOC();
        foc.setPlateNumber(request.getPlateNumber());
        foc.setReceivedBy(request.getReceivedBy());
        foc.setAssignedOfficial(request.getAssignedOfficial());
        foc.setDriverName(request.getDriverName());
        foc.setEntryKm(request.getEntryKm());
        foc.setEntryFuel(request.getEntryFuel());
        foc.setKmDrivenInWorkshop(request.getKmDrivenInWorkshop());
        foc.setPurposeAndDestination(request.getPurposeAndDestination());

        List<OilUsed> oilUsedList = request.getOilUsed().stream()
                .map(oilDto -> {
                    OilUsed oil = new OilUsed();
                    oil.setType(oilDto.getType());
                    oil.setAmount(oilDto.getAmount());
                    return oil;
                })
                .collect(Collectors.toList());

        foc.setOilUsed(oilUsedList);
        foc.setFuelUsed(request.getFuelUsed());
        foc.setExitDate(request.getExitDate());
        foc.setExitKm(request.getExitKm());
        foc.setDispatchOfficer(request.getDispatchOfficer());
        foc.setMechanicName(request.getMechanicName());
        foc.setHeadMechanicName(request.getHeadMechanicName());

        return focRepository.save(foc);
    }
}
