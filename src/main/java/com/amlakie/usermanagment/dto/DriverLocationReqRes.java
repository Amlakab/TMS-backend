package com.amlakie.usermanagment.dto;

public class DriverLocationReqRes {
    private int status;
    private String message;
    private String error;
    private DriverLocationDTO driverLocation;

    // Default constructor
    public DriverLocationReqRes() {}

    // Constructor with status and message
    public DriverLocationReqRes(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public DriverLocationDTO getDriverLocation() { return driverLocation; }
    public void setDriverLocation(DriverLocationDTO driverLocation) { this.driverLocation = driverLocation; }
}