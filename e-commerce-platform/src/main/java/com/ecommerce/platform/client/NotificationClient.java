package com.ecommerce.platform.client;

import com.ecommerce.platform.dto.NotificationChannel;
import com.ecommerce.platform.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    
    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification.service.url}") String notificationServiceUrl,
                              @Value("${notification.service.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl + endpoint;
    }

    public void sendNotification(Long userId, NotificationChannel channel, String message) {
        sendNotification(userId, channel, message, null);
    }

    public void sendNotification(Long userId, NotificationChannel channel, String message, LocalDateTime scheduledAt) {
        try {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setChannel(channel);
            request.setMessage(message);
            request.setScheduledAt(scheduledAt);

            log.info("Sending notification to Notification System: userId={}, channel={}, message={}", 
                    userId, channel, message);
            
            restTemplate.postForObject(notificationServiceUrl, request, Void.class);
            
            log.info("Notification sent successfully to Notification System");
        } catch (Exception e) {
            log.error("Failed to send notification to Notification System", e);
            // Don't throw - we don't want to break e-commerce flow if notification fails
        }
    }
}
