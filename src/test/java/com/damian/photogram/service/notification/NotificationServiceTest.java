package com.damian.photogram.service.notification;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.notification.Notification;
import com.damian.photogram.domain.notification.NotificationRepository;
import com.damian.photogram.domain.notification.NotificationType;
import com.damian.photogram.domain.notification.exception.NotificationSelfNotificationException;
import com.damian.photogram.domain.user.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.CustomerRepository;
import com.damian.photogram.web.rest.notification.dto.NotificationEvent;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest extends AbstractServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("should get notifications")
    void shouldGetNotifications() {
        // given
        Customer customer = Customer.create()
                                    .setId(1L)
                                    .setEmail("publisher@demo.com")
                                    .setPassword(passwordEncoder.encode(RAW_PASSWORD));
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
        Customer customer = Customer.create()
                                    .setId(1L)
                                    .setEmail("publisher@demo.com")
                                    .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        setUpContext(customer);
        notificationService.deleteNotifications();
        verify(notificationRepository).deleteAllByCustomer_Id(customer.getId());
    }

    @Test
    @DisplayName("should get notifications and removes sink on cancel")
    void shouldGetNotificationsAndRemovesSink() throws NoSuchFieldException, IllegalAccessException {
        // given
        Customer customer = Customer.create()
                                    .setId(1L)
                                    .setEmail("publisher@demo.com")
                                    .setPassword(passwordEncoder.encode(RAW_PASSWORD));
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
        Customer publisher = Customer.create()
                                     .setId(1L)
                                     .setEmail("publisher@demo.com")
                                     .setPassword(passwordEncoder.encode(RAW_PASSWORD));
        setUpContext(publisher);

        Customer recipient = Customer.create()
                                     .setId(2L)
                                     .setEmail("recipient@demo.com")
                                     .setPassword(passwordEncoder.encode(RAW_PASSWORD));

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
        Customer publisher = Customer.create()
                                     .setId(1L)
                                     .setEmail("publisher@demo.com")
                                     .setPassword(passwordEncoder.encode(RAW_PASSWORD));

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
        NotificationSelfNotificationException exception = assertThrows(
                NotificationSelfNotificationException.class,
                () -> notificationService.publishNotification(event)
        );

        assertEquals(Exceptions.NOTIFICATION.SELF_NOTIFICATION, exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("should not publish when recipient not found")
    void shouldNotPublishNotificationWhenRecipientNotFound() {
        // given
        Customer publisher = Customer.create()
                                     .setId(1L)
                                     .setEmail("publisher@demo.com")
                                     .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        setUpContext(publisher);

        Map<String, Object> metadata = Map.of(
                "postId", 1L,
                "username", "username"
        );

        NotificationEvent event = new NotificationEvent(
                2L, // same as publisher
                NotificationType.COMMENT,
                metadata,
                "msg",
                Instant.now().toString()
        );

        // when
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> notificationService.publishNotification(event)
        );

        assertEquals(Exceptions.CUSTOMER.NOT_FOUND, exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }

}
