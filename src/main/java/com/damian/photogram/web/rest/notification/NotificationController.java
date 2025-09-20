package com.damian.photogram.web.rest.notification;

import com.damian.photogram.domain.notification.Notification;
import com.damian.photogram.service.notification.NotificationService;
import com.damian.photogram.web.rest.notification.dto.NotificationEvent;
import com.damian.photogram.web.rest.notification.dto.mapper.NotificationDtoMapper;
import com.damian.photogram.web.rest.notification.dto.response.NotificationDto;
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
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // endpoint to fetch (paginated) notifications from current customer
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
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
        notificationService.deleteNotifications();

        return ResponseEntity
                .noContent()
                .build();
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NotificationEvent> streamNotifications() {
        return notificationService.getNotificationsForUser();
    }
}