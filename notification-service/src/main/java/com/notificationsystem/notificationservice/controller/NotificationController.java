package com.notificationsystem.notificationservice.controller;

import com.notificationsystem.notificationservice.dto.NotificationRequest;
import com.notificationsystem.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NotificationController = the HTTP door of the system.
 *
 * Beginner view: this is the only class the CLIENT talks to.
 * URL: POST /notifications
 *
 * A normal web app often returns 200 after the work is finished
 * (email already sent). Here we return 202 Accepted instead:
 * "I received your request. Sending happens later in Kafka."
 */
@RestController // this class returns HTTP responses (JSON / status codes), not HTML pages
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // Spring injects NotificationService for us (constructor injection)
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody NotificationRequest request) {
        // @Valid = check @NotNull / @NotBlank on NotificationRequest first
        // @RequestBody = turn JSON into a Java object
        notificationService.create(request);

        // 202 Accepted = accepted, not done yet. No email has been sent at this moment.
        return ResponseEntity.accepted().build();
    }
}
