package com.damian.photogram.app.notification;

import com.damian.photogram.app.notification.dto.NotificationDto;
import org.springframework.data.domain.Page;

public class NotificationDtoMapper {
    public static NotificationDto map(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getMetadata(),
                notification.getCreatedAt().toString()
        );
    }

    public static Page<NotificationDto> map(Page<Notification> notifications) {
        return notifications
                .map(
                        NotificationDtoMapper::map
                );
    }

}
