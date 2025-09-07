package com.damian.photogram.app.notification;

import com.damian.photogram.app.notification.dto.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    private final RabbitTemplate rabbitTemplate;
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Map<Long, Sinks.Many<NotificationEvent>> userSinks = new ConcurrentHashMap<>();

    public NotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String message) {
        sink.tryEmitNext(message);
    }

    //    public void publish(Object notification) {
    //        rabbitTemplate.convertAndSend(
    //                "appExchange",
    //                "notifications.key",
    //                notification
    //        );
    //    }

    public Flux<String> getStream() {
        return sink.asFlux();
    }

    public Flux<NotificationEvent> getNotificationsForUser(Long userId) {
        return userSinks
                .computeIfAbsent(userId, k -> Sinks.many().multicast().onBackpressureBuffer())
                .asFlux();
    }

    public void publish(NotificationEvent event, Long recipientId) {
        // if the receiverId is the same as senderId then do nothing
        if (event.senderId().equals(recipientId)) {
            return;
        }

        var sink = userSinks.get(recipientId);
        if (sink != null) {
            sink.tryEmitNext(event);
        }
    }
}