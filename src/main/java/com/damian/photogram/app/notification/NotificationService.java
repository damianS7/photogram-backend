package com.damian.photogram.app.notification;

import com.damian.photogram.app.notification.dto.NotificationEvent;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.common.AuthHelper;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final Map<Long, Sinks.Many<NotificationEvent>> userSinks = new ConcurrentHashMap<>();

    public NotificationService(
            NotificationRepository notificationRepository,
            CustomerRepository customerRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get notifications for the current customer.
     *
     * @param pageable pagination params
     * @return Page<Notification> a page of notifications
     */
    public Page<Notification> getNotifications(Pageable pageable) {
        Customer customer = AuthHelper.getLoggedCustomer();
        return notificationRepository.findAllByCustomerId(customer.getId(), pageable);
    }

    /**
     * Delete all notifications for the current customer.
     */
    @Transactional
    public void deleteNotifications() {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // delete all notifications
        notificationRepository.deleteAllByCustomer_Id(currentCustomer.getId());
    }

    /**
     * Get notifications for the current customer as a Flux stream.
     * The stream will be closed when the client disconnects.
     *
     * @return Flux<NotificationEvent> a stream of notifications
     */
    public Flux<NotificationEvent> getNotificationsForUser() {
        Customer customer = AuthHelper.getLoggedCustomer();

        // create a sink for the user if not exists
        Sinks.Many<NotificationEvent> sink = userSinks.computeIfAbsent(
                customer.getId(),
                k -> Sinks.many().multicast().onBackpressureBuffer()
        );

        // remove when disconnect
        return sink.asFlux().doOnCancel(() -> {
            userSinks.remove(customer.getId());
        });
    }

    /**
     * Publish a notification event to the recipient.
     *
     * @param notificationEvent the notification event
     */
    public void publishNotification(NotificationEvent notificationEvent) {
        Customer customer = AuthHelper.getLoggedCustomer();

        // if the receiverId is the same as senderId then do nothing
        // this is to prevent sending notifications to oneself
        // for example when a user likes or comment their own post
        if (customer.getId().equals(notificationEvent.recipientId())) {
            log.debug("Notification not sent: sender {} cannot notify himself.", customer.getId());
            return;
        }

        // find recipient customer who will receive the notification
        Customer recipient = customerRepository
                .findById(notificationEvent.recipientId())
                .orElseThrow(() -> {
                    log.warn(
                            "Notification not sent: Customer recipient with id={} not found.",
                            notificationEvent.recipientId()
                    );
                    return new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND);
                });

        // create and save notification to the database
        Notification notification = Notification
                .create(recipient)
                .setMessage(notificationEvent.message())
                .setMetadata(notificationEvent.metadata())
                .setType(notificationEvent.type());
        notificationRepository.save(notification);
        log.info(
                "Notification for recipientId={} of type={} stored on db.",
                notificationEvent.recipientId(),
                notificationEvent.type()
        );


        // emit event to the recipient if connected
        var sink = userSinks.get(notificationEvent.recipientId());
        if (sink != null) {
            sink.tryEmitNext(notificationEvent);
            log.debug("Notification emitted to connected user recipientId={}", notificationEvent.recipientId());
        }
    }
}