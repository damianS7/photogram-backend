package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.exception.*;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Follow;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.repository.FollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class FollowService {
    private final short MAX_FOLLOWS = 20;
    private final FollowRepository followRepository;
    private final CustomerRepository customerRepository;

    public FollowService(
            FollowRepository followRepository,
            CustomerRepository customerRepository
    ) {
        this.followRepository = followRepository;
        this.customerRepository = customerRepository;
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

        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND);
        }

        return followRepository.findAllByFollowedCustomer_Id(customerId, pageable);
    }

    /**
     * Get all following users from the current customer
     *
     * @param pageable pagination params
     * @return Page<Follow> a page of following users
     */
    public Page<Follow> getFollowed(Pageable pageable) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        return getFollowed(currentCustomer.getId(), pageable);
    }

    /**
     * Get all following customers from a specific customer
     *
     * @param customerId the id of the customer to get following customers from
     * @param pageable   pagination params
     * @return Page<Follow> a page of following users
     * @throws CustomerNotFoundException if the customer is not found
     */
    public Page<Follow> getFollowed(Long customerId, Pageable pageable) {
        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND);
        }

        return followRepository.findAllByFollowerCustomer_Id(customerId, pageable);
    }

    /**
     * Get the Follow entity between the current customer and the customer specified in the customerId.
     * Its used to check if the current customer is following the specified customer.
     *
     * @param customerId the id of the customer to get the follow relationship
     * @return Follow the entity between the current customer and the specified
     * @throws FollowNotFoundException   if the follow relationship does not exist
     * @throws CustomerNotFoundException if the given customer does not exist
     */
    public Follow getFollow(Long customerId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND);
        }

        // check if the follow exists
        return followRepository
                .findFollowRelationshipBetweenCustomers(customerId, currentCustomer.getId())
                .orElseThrow(
                        () -> new FollowNotFoundException(Exceptions.FOLLOW.NOT_FOUND)
                );
    }

    /**
     * It follows a customer.
     * Current customer will follow the specified customer.
     *
     * @param customerId the id of the customer to follow
     * @return Follow the entity between the current customer and the specified customer.
     * @throws FollowersLimitExceededException   if the current customer has reached the maximum number of follows
     * @throws FollowYourselfNotAllowedException if the current customer is trying to follow itself
     * @throws CustomerNotFoundException         if the given customer does not exist
     * @throws FollowAlreadyExistsException      if the specified customer already follows the current customer
     */
    public Follow follow(Long customerId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the currentCustomer can add more following
        if (followRepository.countFollowersFromCustomer(currentCustomer.getId()) >= MAX_FOLLOWS) {
            throw new FollowersLimitExceededException(Exceptions.FOLLOW.MAX_FOLLOWERS);
        }

        // check if the customer we want to add as a follow exists.
        Customer customerToFollow = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        // check if currentCustomer and followedCustomer are not the same customer.
        if (currentCustomer.getId().equals(customerToFollow.getId())) {
            throw new FollowYourselfNotAllowedException(Exceptions.FOLLOW.SELF_FOLLOW);
        }

        // check if customerToFollow is not already following by the currentCustomer
        if (followRepository.isFollowing(customerToFollow.getId(), currentCustomer.getId())) {
            throw new FollowAlreadyExistsException(Exceptions.FOLLOW.ALREADY_EXISTS);
        }

        // save the follow relationship in the database
        return followRepository.save(
                new Follow(customerToFollow, currentCustomer)
        );
    }

    /**
     * Unfollow a customer
     * It will result in the unfollow of the customerId by the current customer
     *
     * @param customerId the ID of the customer to unfollow
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws FollowNotFoundException   if the follow does not exist
     */
    public void unfollow(Long customerId) {
        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND);
        }

        // check if the follow exists
        Follow follow = this.getFollow(customerId);

        // delete the follow relationship from the database
        followRepository.deleteById(follow.getId());
    }
}
