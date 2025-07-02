package com.amlakie.usermanagment.service.util;

import com.amlakie.usermanagment.dto.fogrequest.FillDetailsDTO;
import com.amlakie.usermanagment.dto.fogrequest.FuelOilGreaseRequestDTO;
import com.amlakie.usermanagment.dto.fogrequest.RequestItemDTO;
import com.amlakie.usermanagment.entity.fogrequest.FillDetails;
import com.amlakie.usermanagment.entity.fogrequest.FuelOilGreaseRequest;
import com.amlakie.usermanagment.entity.fogrequest.RequestItem;
import com.amlakie.usermanagment.entity.fogrequest.ApprovalStatus;
import com.amlakie.usermanagment.entity.fogrequest.NezekStatus;
import com.amlakie.usermanagment.entity.fogrequest.RequestStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FuelRequestConverter {

    public static FuelOilGreaseRequestDTO toDto(FuelOilGreaseRequest entity) {
        FuelOilGreaseRequestDTO dto = new FuelOilGreaseRequestDTO();
        dto.setId(entity.getId());
        dto.setRequestDate(entity.getRequestDate() != null ? entity.getRequestDate().toString() : null);
        dto.setCarType(entity.getCarType());
        dto.setPlateNumber(entity.getPlateNumber());
        dto.setKmReading(entity.getKmReading());
        dto.setShortExplanation(entity.getShortExplanation());
        dto.setMechanicName(entity.getMechanicName());
        dto.setHeadMechanicName(entity.getHeadMechanicName());
        // Map ApprovalStatus to Boolean | null: PENDING -> null, APPROVED -> true, REJECTED -> false
        dto.setHeadMechanicApproved(
                entity.getHeadMechanicApproval() == ApprovalStatus.PENDING ? null :
                        entity.getHeadMechanicApproval() == ApprovalStatus.APPROVED ? true : false
        );
        dto.setNezekOfficialName(entity.getNezekOfficialName());
        dto.setNezekOfficialStatus(entity.getNezekStatus());
        dto.setIsFulfilled(entity.getIsFulfilled());
        dto.setStatus(entity.getStatus());

        if (entity.getItems() != null) {
            for (RequestItem item : entity.getItems()) {
                RequestItemDTO itemDTO = toItemDto(item);
                switch (item.getCategory()) {
                    case "fuel":
                        dto.setFuel(itemDTO);
                        break;
                    case "motorOil":
                        dto.setMotorOil(itemDTO);
                        break;
                    case "brakeFluid":
                        dto.setBrakeFluid(itemDTO);
                        break;
                    case "steeringFluid":
                        dto.setSteeringFluid(itemDTO);
                        break;
                    case "grease":
                        dto.setGrease(itemDTO);
                        break;
                }
            }
        }

        return dto;
    }

    public static FuelOilGreaseRequest toEntity(FuelOilGreaseRequestDTO dto) {
        FuelOilGreaseRequest entity = new FuelOilGreaseRequest();
        entity.setId(dto.getId());
        entity.setRequestDate(dto.getRequestDate() != null ? LocalDate.parse(dto.getRequestDate()) : null);
        entity.setCarType(dto.getCarType());
        entity.setPlateNumber(dto.getPlateNumber());
        entity.setKmReading(dto.getKmReading());
        entity.setShortExplanation(dto.getShortExplanation());
        entity.setMechanicName(dto.getMechanicName());
        entity.setHeadMechanicName(dto.getHeadMechanicName());
        entity.setHeadMechanicApproval(
                dto.getHeadMechanicApproved() == null ? ApprovalStatus.PENDING :
                        dto.getHeadMechanicApproved() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED
        );
        entity.setNezekOfficialName(dto.getNezekOfficialName());
        entity.setNezekStatus(dto.getNezekOfficialStatus() != null ? dto.getNezekOfficialStatus() : NezekStatus.PENDING);
        entity.setIsFulfilled(dto.getIsFulfilled() != null ? dto.getIsFulfilled() : false);
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : RequestStatus.DRAFT);

        List<RequestItem> items = new ArrayList<>();
        if (dto.getFuel() != null) {
            items.add(toItemEntity(dto.getFuel(), "fuel", entity));
        }
        if (dto.getMotorOil() != null) {
            items.add(toItemEntity(dto.getMotorOil(), "motorOil", entity));
        }
        if (dto.getBrakeFluid() != null) {
            items.add(toItemEntity(dto.getBrakeFluid(), "brakeFluid", entity));
        }
        if (dto.getSteeringFluid() != null) {
            items.add(toItemEntity(dto.getSteeringFluid(), "steeringFluid", entity));
        }
        if (dto.getGrease() != null) {
            items.add(toItemEntity(dto.getGrease(), "grease", entity));
        }
        entity.setItems(items);

        return entity;
    }

    public static RequestItemDTO toItemDto(RequestItem item) {
        RequestItemDTO dto = new RequestItemDTO();
        dto.setId(item.getId());
        dto.setType(item.getType());
        dto.setRequested(toFillDetailsDto(item.getRequested()));
        dto.setFilled(toFillDetailsDto(item.getFilled()));
        dto.setDetails(item.getDetails());
        return dto;
    }

    public static RequestItem toItemEntity(RequestItemDTO dto, String category, FuelOilGreaseRequest request) {
        RequestItem item = new RequestItem();
        item.setId(dto.getId());
        item.setCategory(category);
        item.setType(dto.getType());
        item.setRequested(toFillDetailsEntity(dto.getRequested()));
        item.setFilled(toFillDetailsEntity(dto.getFilled()));
        item.setDetails(dto.getDetails());
        item.setRequest(request);
        return item;
    }

    public static FillDetailsDTO toFillDetailsDto(FillDetails entity) {
        if (entity == null) return new FillDetailsDTO();
        FillDetailsDTO dto = new FillDetailsDTO();
        dto.setMeasurement(entity.getMeasurement());
        dto.setAmount(entity.getAmount());
        dto.setPrice(entity.getPrice());
        return dto;
    }

    public static FillDetails toFillDetailsEntity(FillDetailsDTO dto) {
        if (dto == null) return new FillDetails();
        return new FillDetails(dto.getMeasurement(), dto.getAmount(), dto.getPrice());
    }

    public static List<RequestItemDTO> extractItemDTOs(FuelOilGreaseRequestDTO dto) {
        List<RequestItemDTO> items = new ArrayList<>();
        if (dto.getFuel() != null) {
            items.add(dto.getFuel());
        }
        if (dto.getMotorOil() != null) {
            items.add(dto.getMotorOil());
        }
        if (dto.getBrakeFluid() != null) {
            items.add(dto.getBrakeFluid());
        }
        if (dto.getSteeringFluid() != null) {
            items.add(dto.getSteeringFluid());
        }
        if (dto.getGrease() != null) {
            items.add(dto.getGrease());
        }
        return items;
    }
}