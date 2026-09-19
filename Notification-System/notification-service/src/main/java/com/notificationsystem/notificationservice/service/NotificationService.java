package com.notificationsystem.notificationservice.service;

import com.notificationsystem.notificationservice.domain.Notification;
import com.notificationsystem.notificationservice.dto.NotificationRequest;
import com.notificationsystem.notificationservice.event.NotificationEvent;
import com.notificationsystem.notificationservice.model.NotificationStatus;
import com.notificationsystem.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NotificationService = the brain of "create a notification".
 *
 * Beginner view: the controller should stay thin. This class does the real steps:
 * 1) save a row in MySQL (status PENDING)
 * 2) if the time is now (or already past) → publish to Kafka
 * 3) if the time is in the future → do nothing; NotificationScheduler will publish later
 *
 * KafkaTemplate is the Kafka PRODUCER (the sender).
 */
@Service
public class NotificationService {

    // KafkaTemplate<key type, value type>
    // key = String (userId)   value = NotificationEvent (JSON)
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final NotificationRepository notificationRepository;
    private final String notificationsTopic;

    public NotificationService(
            KafkaTemplate<String, NotificationEvent> kafkaTemplate,
            NotificationRepository notificationRepository,
            @Value("${notification.kafka.topics.notifications}") String notificationsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationRepository = notificationRepository;
        this.notificationsTopic = notificationsTopic;
    }

    @Transactional // save to DB as one unit of work
    public void create(NotificationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString()); // unique id for DB + Kafka event
        notification.setUserId(request.getUserId());
        notification.setChannel(request.getChannel());
        notification.setMessage(request.getMessage());
        notification.setStatus(NotificationStatus.PENDING);
        // No scheduledAt from client → treat as "send now"
        notification.setScheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : now);
        notification.setCreatedAt(now);
        notificationRepository.save(notification);

        // isAfter(now) means the time is still in the future → wait for the scheduler
        if (!notification.getScheduledAt().isAfter(now)) {
            //in other words if(notification.getScheduledAt()<=now)
            publish(notification);
        }
    }

    /**
     * Put the job on Kafka topic "notifications".
     * Called from create() for immediate jobs, and from the scheduler for future jobs.
     */
    @Transactional
    public void publish(Notification notification) {
        // Claim the row: PENDING → PROCESSING. claimed == 0 means someone else already took it.
        int claimed = notificationRepository.updateStatusIfCurrent(
                notification.getId(),
                NotificationStatus.PENDING,
                NotificationStatus.PROCESSING
        );
        if (claimed == 0) {
            return;
        }

        NotificationEvent event = new NotificationEvent(
                notification.getId(),
                notification.getUserId(),
                notification.getChannel(),
                notification.getMessage()
        );

        // IMPORTANT: the second argument is the Kafka KEY.
        // Same userId always goes to the same partition → messages for one user stay in order.
        kafkaTemplate.send(
                notificationsTopic,
                notification.getUserId().toString(),
                event
        );
    }
}
