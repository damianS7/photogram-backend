package com.damian.photogram.app.notification;

import com.damian.photogram.core.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {
    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void receiveMessage(String message) {
        //        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        //        System.out.println(message);
        notificationService.publish(message);
    }
}