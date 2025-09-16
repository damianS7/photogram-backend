package com.damian.photogram.service.user;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.notification.NotificationType;
import com.damian.photogram.domain.user.exception.*;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Follow;
import com.damian.photogram.domain.user.repository.CustomerRepository;
import com.damian.photogram.domain.user.repository.FollowRepository;
import com.damian.photogram.service.notification.NotificationService;
import com.damian.photogram.web.notification.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Manages follow relationships between customers in the application.
 * This service provides functionality to:
 * — Follow/unfollow other customers.
 * — Get followers and following lists.
 * — Check follow relationships.
 */
@Service
public class FollowService {
    private static final Logger log = LoggerFactory.getLogger(FollowService.class);
    private final int MAX_FOLLOWS = 999;
    private final FollowRepository followRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public FollowService(
            FollowRepository followRepository,
            CustomerRepository customerRepository,
            NotificationService notificationService
    ) {
        this.followRepository = followRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get all followers for the current customer.
     *
     * @param pageable pagination params
     * @return Page<Follow> a page of followers
     */
    public Page<Follow> getFollowers(Pageable pageable) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        return getFollowers(currentCustomer.getId(), pageable);
    }

    /**
     * Get all followers from a customer by its customerId.
     *
     * @param customerId id of the customer to get followers from
     * @param pageable   pagination params
     * @return Page<Follow> a page of followers
     * @throws CustomerNotFoundException if the customer is not found
     */
    public Page<Follow> getFollowers(Long customerId, Pageable pageable) {
        log.debug("Fetching followers from customerId: {}", customerId);

        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND, customerId);
        }

        return followRepository.findAllByFollowedCustomer_Id(customerId, pageable);
    }

    /**
     * Get all the customers following the currentCustomer
     *
     * @param pageable pagination params
     * @return Page<Follow> a page of following users
     */
    public Page<Follow> getFollowing(Pageable pageable) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        return getFollowing(currentCustomer.getId(), pageable);
    }

    /**
     * Get all the customers following the given customerId
     *
     * @param customerId the id of the customer to get following customers from
     * @param pageable   pagination params
     * @return Page<Follow> a page of following users
     * @throws CustomerNotFoundException if the customer is not found
     */
    public Page<Follow> getFollowing(Long customerId, Pageable pageable) {
        log.debug("Fetching all the customers being followed by customerId: {}", customerId);

        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND, customerId);
        }

        return followRepository.findAllByFollowerCustomer_Id(customerId, pageable);
    }

    /**
     * Get the Follow entity between the current customer, and the customer specified in the customerId.
     * It is used to check if the current customer is after the specified customer.
     *
     * @param customerId the id of the customer to get the follow relationship
     * @return Follow the entity between the current customer, and the specified
     * @throws FollowBetweenUsersNotExistException if the follow relationship does not exist
     * @throws CustomerNotFoundException           if the given customer does not exist
     */
    public Follow getFollow(Long customerId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug(
                "Fetching follow entity between customerId: {} and customerId: {}",
                currentCustomer.getId(),
                customerId
        );

        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND, customerId);
        }

        // check if the follow exists
        return followRepository
                .findFollowRelationshipBetweenCustomers(currentCustomer.getId(), customerId)
                .orElseThrow(
                        () -> new FollowBetweenUsersNotExistException(
                                Exceptions.FOLLOW.NOT_FOUND,
                                currentCustomer.getId(),
                                customerId
                        )
                );
    }

    /**
     * Current customer will follow the specified customer.
     *
     * @param customerToFollowId the id of the customer to follow
     * @return Follow the entity between the current customer and the specified customer.
     * @throws FollowersLimitExceededException   if the current customer has reached the maximum number of follows
     * @throws FollowYourselfNotAllowedException if the current customer is trying to follow itself
     * @throws CustomerNotFoundException         if the given customer does not exist
     * @throws FollowAlreadyExistsException      if the specified customer already follows the current customer
     */
    public Follow follow(Long customerToFollowId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("customer: {} attempt to follow customer: {}", currentCustomer.getId(), customerToFollowId);

        // check if the currentCustomer can add more following
        if (followRepository.countFollowersFromCustomer(currentCustomer.getId()) >= MAX_FOLLOWS) {
            throw new FollowersLimitExceededException(
                    Exceptions.FOLLOW.MAX_FOLLOWERS,
                    currentCustomer.getId(),
                    customerToFollowId
            );
        }

        // check if the customer we want to add as a follow exists.
        Customer customerToFollow = customerRepository.findById(customerToFollowId).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND, customerToFollowId)
        );

        // check if currentCustomer and followedCustomer are not the same customer.
        if (currentCustomer.getId().equals(customerToFollow.getId())) {
            throw new FollowYourselfNotAllowedException(
                    Exceptions.FOLLOW.SELF_FOLLOW,
                    currentCustomer.getId(),
                    customerToFollow.getId()
            );
        }

        // check if customerToFollow is not already following by the currentCustomer
        if (followRepository.isFollowing(currentCustomer.getId(), customerToFollow.getId())) {
            throw new FollowAlreadyExistsException(
                    Exceptions.FOLLOW.ALREADY_EXISTS,
                    currentCustomer.getId(),
                    customerToFollow.getId()
            );
        }

        log.debug("customer: {} now follows customer: {}", currentCustomer.getId(), customerToFollowId);
        // save the follow relationship in the database
        return followRepository.save(
                Follow.create()
                      .follower(currentCustomer)
                      .follows(customerToFollow)
        );
    }

    /**
     * Unfollow a customer
     * It will result in the unfollow of the customerId by the current customer
     *
     * @param customerId the ID of the customer to unfollow
     * @throws CustomerNotFoundException if the customer does not exist
     */
    public void unfollow(Long customerId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("customer: {} attempt to unfollow customer: {}", currentCustomer.getId(), customerId);

        // check if the follow exists
        Follow follow = this.getFollow(customerId);

        // delete the follow relationship from the database
        followRepository.deleteById(follow.getId());
        log.debug("customer: {} unfollow customer: {}", follow.getFollowerCustomer().getId(), customerId);
    }

    /**
     * Send a follow notification to the followed customer
     *
     * @param follow the follow relationship
     */
    public void sendFollowNotification(Follow follow) {
        final String followedUsername = follow.getFollowedCustomer().getProfile().getUsername();
        Map<String, Object> metadata = Map.of(
                "username", followedUsername
        );

        // create the notification event
        NotificationEvent notification = new NotificationEvent(
                follow.getFollowedCustomer().getId(),
                NotificationType.FOLLOW,
                metadata,
                followedUsername + " has follow you.",
                follow.getCreatedAt().toString()
        );

        // publish the notification
        notificationService.publishNotification(notification);
    }
}
