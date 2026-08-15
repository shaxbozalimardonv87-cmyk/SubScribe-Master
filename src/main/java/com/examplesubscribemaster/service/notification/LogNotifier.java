package com.examplesubscribemaster.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogNotifier implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(LogNotifier.class);

    public void notifyUser(String r, String s, String m) {
        log.info("Notification to {}: {} - {}", r, s, m);
    }
}
