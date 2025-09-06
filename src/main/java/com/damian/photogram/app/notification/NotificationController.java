package com.damian.photogram.app.notification;

import com.damian.photogram.app.notification.dto.NotificationEvent;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.model.Customer;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    //    public Flux<String> streamNotifications() {
    //        return notificationService.getStream();
    //    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEvent> streamNotifications(Authentication auth) {
        Customer customer = AuthHelper.getLoggedCustomer();
        //        String userId = ((Customer) auth.getPrincipal()).getId().toString();
        return notificationService.getNotificationsForUser(customer.getId());
    }
}