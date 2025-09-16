package com.damian.photogram.domain.notification;

import com.damian.photogram.service.notification.NotificationService;
import com.damian.photogram.core.config.RabbitConfig;
import com.damian.photogram.web.notification.dto.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Responsible for consuming notification messages from a RabbitMQ queue.
 */
@Service
public class NotificationConsumer {
    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Listens for messages from the RabbitMQ queue and processes them.
     *
     * @param notification The notification event received from the queue.
     */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receiveMessage(NotificationEvent notification) {
        // Delegates the processing of the notification to the NotificationService
        notificationService.publishNotification(notification);
    }
}