package com.examplesubscribemaster.service.notification;

public interface NotificationService {
    void notifyUser(String recipient, String subject, String message);
}
