package com.damian.photogram.app.notification;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.app.notification.dto.NotificationEvent;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest extends AbstractServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private Customer mockCustomer;

    @Test
    @DisplayName("should get notifications")
    void shouldGetNotifications() {
        // given
        Customer customer = new Customer(
                1L, "publisher@demo.com", "1223456"
        );

        setUpContext(customer);

        // when
        Pageable pageable = mock(Pageable.class);
        Page<Notification> page = new PageImpl<>(
                List.of(
                        Notification.create(customer),
                        Notification.create(customer),
                        Notification.create(customer)
                )
        );
        when(notificationRepository.findAllByCustomerId(customer.getId(), pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getNotifications(pageable);

        // then
        assertThat(result).isEqualTo(page);
        assertThat(result.getTotalElements()).isEqualTo(page.getTotalElements());
        verify(notificationRepository).findAllByCustomerId(customer.getId(), pageable);
    }

    @Test
    @DisplayName("should delete notifications")
    void shouldDeleteNotifications() {
        // given
        Customer customer = new Customer(
                1L, "publisher@demo.com", "1223456"
        );

        setUpContext(customer);
        notificationService.deleteNotifications();
        verify(notificationRepository).deleteAllByCustomer_Id(customer.getId());
    }

    @Test
    @DisplayName("should get notifications and removes sink on cancel")
    void shouldGetNotificationsAndRemovesSink() throws NoSuchFieldException, IllegalAccessException {
        // given
        Customer customer = new Customer(
                1L, "publisher@demo.com", "1223456"
        );
        setUpContext(customer);

        Flux<NotificationEvent> flux = notificationService.getNotificationsForUser();

        // Usar StepVerifier para cancelar el Flux y verificar que el sink se elimina
        reactor.test.StepVerifier.create(flux)
                                 .thenCancel()
                                 .verify();

        // Access the private userSinks field using reflection
        java.lang.reflect.Field field = NotificationService.class.getDeclaredField("userSinks");
        field.setAccessible(true);
        Map<Long, ?> sinks = (Map<Long, ?>) field.get(notificationService);

        // Check that the sink for the user has been removed after cancellation
        assertThat(sinks.get(1L)).isNull();
    }

    @Test
    @DisplayName("should publish notification")
    void shouldPublishNotification() {
        // given
        Customer publisher = new Customer(
                1L, "publisher@demo.com", "1223456"
        );
        setUpContext(publisher);

        Customer recipient = new Customer(
                2L, "recipient@demo.com", "1223456"
        );

        Map<String, Object> metadata = Map.of(
                "postId", 3L,
                "username", "username"
        );

        NotificationEvent event = new NotificationEvent(
                recipient.getId(),
                NotificationType.COMMENT,
                metadata,
                "msg",
                Instant.now().toString()
        );

        // when
        when(customerRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any()))
                .thenAnswer(i -> i.getArguments()[0]);
        notificationService.publishNotification(event);

        // then
        verify(notificationRepository).save(any());
    }

    @Test
    @DisplayName("should not publish when publisher and recipient are the same")
    void shouldNotPublishNotificationWhenPublisherAndRecipientAreTheSame() {
        // given
        Customer publisher = new Customer(
                1L, "publisher@demo.com", "1223456"
        );
        setUpContext(publisher);

        Map<String, Object> metadata = Map.of(
                "postId", 1L,
                "username", "username"
        );

        NotificationEvent event = new NotificationEvent(
                publisher.getId(), // same as publisher
                NotificationType.COMMENT,
                metadata,
                "msg",
                Instant.now().toString()
        );
        // when
        notificationService.publishNotification(event);
        verify(notificationRepository, never()).save(any());
    }

}
