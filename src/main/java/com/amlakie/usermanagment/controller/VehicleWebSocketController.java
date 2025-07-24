package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.VehicleLocationResponseDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleWebSocketController {

    @MessageMapping("/vehicle-locations")
    @SendTo("/topic/vehicle-updates")
    public VehicleLocationResponseDTO handleVehicleUpdate(VehicleLocationResponseDTO locationUpdate) {
        // This will broadcast the update to all subscribed clients
        return locationUpdate;
    }
}