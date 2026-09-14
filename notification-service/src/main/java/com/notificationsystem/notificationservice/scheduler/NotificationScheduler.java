package com.notificationsystem.notificationservice.scheduler;

import com.notificationsystem.notificationservice.domain.Notification;
import com.notificationsystem.notificationservice.model.NotificationStatus;
import com.notificationsystem.notificationservice.repository.NotificationRepository;
import com.notificationsystem.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationScheduler = a clock that looks at the database every second.
 *
 * Beginner view: HTTP requests only happen when a client calls us.
 * A scheduled notification has no client at 10:00, so we need a timer:
 *
 *   every 1 second → find PENDING rows where scheduledAt <= now → publish to Kafka
 *
 * Needs @EnableScheduling on NotificationServiceApplication, otherwise this never runs.
 *
 * Later we may replace this simple loop with Redis. For learning, @Scheduled is enough.
 */
@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(
            NotificationRepository notificationRepository,
            NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    // fixedDelay = 1000 → wait 1 second after the previous run FINISHES, then run again
    @Scheduled(fixedDelay = 1000)
    public void processScheduledNotifications() {
        List<Notification> due = notificationRepository.findByStatusAndScheduledAtLessThanEqual(
                NotificationStatus.PENDING,
                LocalDateTime.now()
        );

        for (Notification notification : due) {
            log.info("Publishing scheduled notification {}", notification.getId());
            // publish() will skip the row if it is no longer PENDING (safe if two runs overlap)
            notificationService.publish(notification);
        }
    }
}
