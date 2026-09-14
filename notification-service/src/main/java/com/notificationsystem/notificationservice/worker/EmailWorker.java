package com.notificationsystem.notificationservice.worker;

import com.notificationsystem.notificationservice.event.NotificationEvent;
import com.notificationsystem.notificationservice.service.NotificationDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * EmailWorker = the last step for EMAIL notifications.
 *
 * Beginner view: this is the "email provider" for now.
 * A real system would call Gmail / SendGrid / SES here.
 * We only print to the console so you can see the full flow working:
 *
 *   API → Kafka → Processor → this class → mark SENT in MySQL
 *
 * Different groupId from the Processor ("email-worker") so this team
 * only reads the email topic, not the main "notifications" topic.
 */
@Service
public class EmailWorker {

    private static final Logger log = LoggerFactory.getLogger(EmailWorker.class);

    private final NotificationDeliveryService deliveryService;

    public EmailWorker(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = "${notification.kafka.topics.email}", groupId = "email-worker")
    public void sendEmail(NotificationEvent event) {
        //this is not real email sending, only a simulation of sending email
        log.info("Sending email to user {}", event.getUserId());
        // Fake provider: later replace this with a real email API call
        System.out.println("Sending email to user " + event.getUserId());
        deliveryService.markSent(event.getId());
    }
}
