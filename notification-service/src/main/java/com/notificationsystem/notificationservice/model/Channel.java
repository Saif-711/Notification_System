package com.notificationsystem.notificationservice.model;

/**
 * Channel = HOW we send the notification.
 *
 * Beginner view: this is a fixed list of choices, not free text.
 * JSON from the client looks like: "channel": "EMAIL"
 *
 * Why an enum? So nobody can send "WHATSAPP" by accident until we add it on purpose.
 */
public enum Channel {
    EMAIL,   // send through the email topic + EmailWorker
    SMS,     // send through the sms topic + SmsWorker
    WEBHOOK  // POST to another server later (WebhookWorker)
}
