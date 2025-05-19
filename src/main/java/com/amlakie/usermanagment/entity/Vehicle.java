package com.amlakie.usermanagment.entity;

public interface Vehicle {
    Long getId();

    // Method to get the plate number, useful for various operations
    String getPlateNumber();

    Double getCurrentKm();

    void setCurrentKm(Double currentKm);

    String getDriverName();

    float getKmPerLiter();
}

    