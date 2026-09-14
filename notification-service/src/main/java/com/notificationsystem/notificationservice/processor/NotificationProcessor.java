package com.notificationsystem.notificationservice.processor;

import com.notificationsystem.notificationservice.event.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * NotificationProcessor = the ROUTER.
 *
 * Beginner view: it does NOT send email. It only reads from topic "notifications"
 * and forwards the same event to the matching channel topic:
 *
 *   EMAIL   → email-notifications
 *   SMS     → sms-notifications
 *   WEBHOOK → webhook-notifications
 *
 * Why extra hop? So each worker can scale alone (many email workers, few SMS workers).
 *
 * @KafkaListener = this method is a Kafka CONSUMER. Spring calls it for each new message.
 */
@Service
public class NotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final String emailTopic;
    private final String smsTopic;
    private final String webhookTopic;

    public NotificationProcessor(
            KafkaTemplate<String, NotificationEvent> kafkaTemplate,
            @Value("${notification.kafka.topics.email}") String emailTopic,
            @Value("${notification.kafka.topics.sms}") String smsTopic,
            @Value("${notification.kafka.topics.webhook}") String webhookTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.emailTopic = emailTopic;
        this.smsTopic = smsTopic;
        this.webhookTopic = webhookTopic;
    }

    // groupId = team name of consumers. Several instances with the SAME group share the work.
    @KafkaListener(topics = "${notification.kafka.topics.notifications}",
            groupId = "notification-processor")
    public void process(NotificationEvent event) {
        log.info("Processing notification {}", event);

        switch (event.getChannel()) {
            // Keep userId as Kafka key so ordering per user continues on the next topic too
            case EMAIL -> kafkaTemplate.send(emailTopic, event.getUserId().toString(), event);
            case SMS -> kafkaTemplate.send(smsTopic, event.getUserId().toString(), event);
            case WEBHOOK -> kafkaTemplate.send(webhookTopic, event.getUserId().toString(), event);
            default -> log.warn("Unknown channel {} for notification {}", event.getChannel(), event);
        }
    }
}
