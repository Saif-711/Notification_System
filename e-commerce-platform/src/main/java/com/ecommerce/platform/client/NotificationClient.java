package com.ecommerce.platform.client;

import com.ecommerce.platform.dto.NotificationChannel;
import com.ecommerce.platform.dto.NotificationRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final Validator validator;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification.service.url}") String notificationServiceUrl,
                              @Value("${notification.service.endpoint}") String endpoint,
                              Validator validator) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl + endpoint;
        this.validator = validator;
    }

    public void sendNotification(Long userId, NotificationChannel channel, String message) {
        sendNotification(userId, channel, message, null);
    }

    @Retryable(
            value = {RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000),
            recover = "sendNotificationRecovery"
    )
    public void sendNotification(Long userId, NotificationChannel channel, String message, LocalDateTime scheduledAt) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setChannel(channel);
        request.setMessage(message);
        request.setScheduledAt(scheduledAt);

        // Validate using Bean Validation
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Validation failed");
            throw new IllegalArgumentException("Notification request validation failed: " + errorMessage);
        }

        try {
            log.info("Sending notification to Notification System: userId={}, channel={}, message={}",
                    userId, channel, message);

            restTemplate.postForObject(notificationServiceUrl, request, Void.class);

            log.info("Notification sent successfully to Notification System");
        } catch (Exception e) {
            log.error("Failed to send notification to Notification System", e);
            // Don't throw - we don't want to break e-commerce flow if notification fails
        }
    }

    private void sendNotificationRecovery(Long userId, NotificationChannel channel, String message, LocalDateTime scheduledAt, RestClientException e) {
        log.error("Failed to send notification after 3 retry attempts: userId={}, channel={}, error={}",
                userId, channel, e.getMessage());
        // Could queue the notification for later processing here
    }
}
