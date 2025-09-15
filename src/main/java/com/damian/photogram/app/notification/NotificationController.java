package com.damian.photogram.app.notification;

import com.damian.photogram.app.notification.dto.NotificationDto;
import com.damian.photogram.app.notification.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // endpoint to fetch (paginated) notifications from current customer
    @GetMapping("/notifications")
    public ResponseEntity<?> getCustomerNotifications(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("Received request to get customer notifications.");
        Page<Notification> notifications = notificationService.getNotifications(pageable);
        Page<NotificationDto> notificationsDto = NotificationDtoMapper.map(notifications);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationsDto);
    }

    // endpoint to delete a notifications
    @DeleteMapping("/notifications")
    public ResponseEntity<?> deleteNotifications(
    ) {
        log.debug("Received request to delete notifications.");
        notificationService.deleteNotifications();

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEvent> streamNotifications() {
        log.debug("Received request to fetch stream notifications.");
        return notificationService.getNotificationsForUser();
    }
}