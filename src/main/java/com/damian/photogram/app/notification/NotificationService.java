package com.damian.photogram.app.notification;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class NotificationService {
    private final RabbitTemplate rabbitTemplate;
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public NotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String message) {
        sink.tryEmitNext(message);
    }

    public void publish(Object notification) {
        rabbitTemplate.convertAndSend(
                "appExchange",
                "notifications.key",
                notification
        );
    }

    public Flux<String> getStream() {
        return sink.asFlux();
    }
}