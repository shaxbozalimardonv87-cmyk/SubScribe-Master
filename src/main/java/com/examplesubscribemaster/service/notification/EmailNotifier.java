package com.examplesubscribemaster.service.notification;

public class EmailNotifier implements NotificationService {
    public void notifyUser(String recipient, String subject, String message) {
        throw new UnsupportedOperationException("Email provider is intentionally not configured");
    }
}
