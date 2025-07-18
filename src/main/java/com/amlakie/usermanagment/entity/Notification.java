package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String message;
    private String link;

    @Column(name = "is_read")
    private boolean read = false;
    private String role;

    private LocalDateTime createdAt = LocalDateTime.now();
}