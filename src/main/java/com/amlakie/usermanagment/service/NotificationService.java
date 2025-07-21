package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.NotificationDto;
import com.amlakie.usermanagment.entity.Notification;
import com.amlakie.usermanagment.repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;

    public Notification createNotification(NotificationDto notificationDto) {
        Notification notification = new Notification();
        notification.setMessage(notificationDto.getMessage());
        notification.setLink(notificationDto.getLink());
        notification.setRole(notificationDto.getRole());
        return notificationRepo.save(notification);
    }

    public List<NotificationDto> getUnreadNotifications() {
        return notificationRepo.findByReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> getAllNotifications() {
        return notificationRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void markAsRead(String id) {
        notificationRepo.markAsRead(id);
    }

    public void markAllAsRead() {
        notificationRepo.markAllAsRead();
    }

    public Long getUnreadCount() {
        return notificationRepo.countByReadFalse();
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setLink(notification.getLink());
        dto.setRead(notification.isRead());
        dto.setRole(notification.getRole());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}