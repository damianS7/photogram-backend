package com.damian.photogram.domain.notification.exception;

public class NotificationSelfNotificationException extends NotificationException {
    public NotificationSelfNotificationException(String message, Long customerId) {
        super(message, null, customerId);
    }
}
