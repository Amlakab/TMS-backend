// NotificationController.java
package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.NotificationDto;
import com.amlakie.usermanagment.dto.ReqRes;
import com.amlakie.usermanagment.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ReqRes> getNotifications() {
        ReqRes response = new ReqRes();
        try {
            List<NotificationDto> notifications = notificationService.getAllNotifications();
            response.setStatus(200);
            response.setMessage("Notifications retrieved successfully");
            response.setNotifications(notifications);
        } catch (Exception e) {
            response.setStatus(500);
            response.setError(e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<ReqRes> getUnreadNotifications() {
        ReqRes response = new ReqRes();
        try {
            List<NotificationDto> notifications = notificationService.getUnreadNotifications();
            response.setStatus(200);
            response.setMessage("Unread notifications retrieved successfully");
            response.setNotifications(notifications);
        } catch (Exception e) {
            response.setStatus(500);
            response.setError(e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/count")
    public ResponseEntity<ReqRes> getUnreadCount() {
        ReqRes response = new ReqRes();
        try {
            Long count = notificationService.getUnreadCount();
            response.setStatus(200);
            response.setMessage("Unread count retrieved successfully");
            response.setCount(count);
        } catch (Exception e) {
            response.setStatus(500);
            response.setError(e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    public ResponseEntity<ReqRes> createNotification(@RequestBody NotificationDto notificationDto) {
        ReqRes response = new ReqRes();
        try {
            notificationService.createNotification(notificationDto);
            response.setStatus(201);
            response.setMessage("Notification created successfully");
        } catch (Exception e) {
            response.setStatus(500);
            response.setError(e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/mark-as-read/{id}")
    public ResponseEntity<ReqRes> markAsRead(@PathVariable String id) {
        ReqRes response = new ReqRes();
        try {
            notificationService.markAsRead(id);
            response.setStatus(200);
            response.setMessage("Notification marked as read");
        } catch (Exception e) {
            response.setStatus(500);
            response.setError(e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ReqRes> markAllAsRead() {
        ReqRes response = new ReqRes();
        try {
            notificationService.markAllAsRead();
            response.setStatus(200);
            response.setMessage("All notifications marked as read");
        } catch (Exception e) {
            response.setStatus(500);
            response.setError(e.getMessage());
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}