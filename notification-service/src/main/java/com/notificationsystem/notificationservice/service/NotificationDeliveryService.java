package com.notificationsystem.notificationservice.service;

import com.notificationsystem.notificationservice.domain.Notification;
import com.notificationsystem.notificationservice.model.NotificationStatus;
import com.notificationsystem.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * NotificationDeliveryService = update the database AFTER a worker "sends".
 *
 * Beginner view: EmailWorker / SmsWorker / WebhookWorker should not each copy
 * the same "set status = SENT" code. They all call this class.
 *
 * This is how we know later: yes, this notification left the system, and when.
 */
@Service
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;

    public NotificationDeliveryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void markSent(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + id));
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
