package com.notificationsystem.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is the START of the whole Spring Boot app.
 *
 * Beginner view: when you run this class, Spring starts a web server,
 * connects to MySQL and Kafka, and creates all other classes (controller, workers, scheduler).
 *
 * Unlike a normal website that only answers HTTP requests, this app also:
 * - listens to Kafka topics (background workers)
 * - runs a timer (scheduler) for future notifications
 */
@SpringBootApplication // turns this into a Spring Boot app and scans this package for other classes
@EnableScheduling      // allows @Scheduled methods (see NotificationScheduler) to run automatically
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
