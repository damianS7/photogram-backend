package com.damian.photogram.domain.notification.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class NotificationException extends ApplicationException {
    private final Long notificationId;
    private final Long customerId;

    public NotificationException(String message) {
        this(message, null, null);
    }

    public NotificationException(String message, Long notificationId, Long customerId) {
        super(message);
        this.customerId = customerId;
        this.notificationId = notificationId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
