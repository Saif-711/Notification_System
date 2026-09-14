package com.notificationsystem.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaTopicConfig = create the Kafka "mailboxes" (topics) when the app starts.
 *
 * Beginner view: a topic is a named queue.
 * We have 4 mailboxes:
 * - notifications          → everyone lands here first
 * - email-notifications    → only email jobs
 * - sms-notifications       → only SMS jobs
 * - webhook-notifications   → only webhook jobs
 *
 * partitions(3): one topic can be split so several workers can share the load.
 * replicas(1): one copy only (fine for learning; production uses more copies).
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationsTopic(
            @Value("${notification.kafka.topics.notifications}") String topic) {
        return TopicBuilder
                .name(topic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailNotificationsTopic(
            @Value("${notification.kafka.topics.email}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic smsNotificationsTopic(
            @Value("${notification.kafka.topics.sms}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic webhookNotificationsTopic(
            @Value("${notification.kafka.topics.webhook}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
