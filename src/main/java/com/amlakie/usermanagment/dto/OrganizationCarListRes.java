package com.amlakie.usermanagment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrganizationCarListRes {
    private List<OrganizationCarReqRes> cars;

    public OrganizationCarListRes(List<OrganizationCarReqRes> cars) {
        this.cars = cars;
    }

}