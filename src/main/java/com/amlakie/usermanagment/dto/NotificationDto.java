package com.amlakie.usermanagment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private String id;
    private String message;
    private String link;
    private boolean read;
    private String role;
    private LocalDateTime createdAt;
}