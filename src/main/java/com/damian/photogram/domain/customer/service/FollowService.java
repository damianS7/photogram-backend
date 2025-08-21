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
    private final short MAX_FOLLOWS = 10;
    private final FollowRepository followRepository;
    private final CustomerRepository customerRepository;

    public FollowService(
            FollowRepository followRepository,
            CustomerRepository customerRepository
    ) {
        this.followRepository = followRepository;
        this.customerRepository = customerRepository;
    }

    // get all the followers for the logged customer
    public Page<Follow> getFollowers(Pageable pageable) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return getFollowers(loggedCustomer.getId(), pageable);
    }

    // get all the followers from a specific customer
    public Page<Follow> getFollowers(Long customerId, Pageable pageable) {

        // TODO why does not do existsById in getFollowed?
        // check if the customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND);
        }

        return followRepository.findAllByFollowedCustomer_Id(customerId, pageable);
    }

    // get all the following users by the logged customer
    public Page<Follow> getFollowed(Pageable pageable) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return getFollowed(loggedCustomer.getId(), pageable);
    }

    // get all the following users from a specific customer
    public Page<Follow> getFollowed(Long customerId, Pageable pageable) {
        return followRepository.findAllByFollowerCustomer_Id(customerId, pageable);
    }

    // get the Follow entity between the logged customer and the customer specified in the id.
    public Follow getFollow(Long customerId) {
        Customer followerCustomer = AuthHelper.getLoggedCustomer();

        // check if the follow exists
        return followRepository
                .findFollowRelationshipBetweenCustomers(customerId, followerCustomer.getId())
                .orElseThrow(
                        () -> new FollowNotFoundException(Exceptions.FOLLOW.NOT_FOUND)
                );
    }

    // followedCustomerId is the id of the customer the logged customer will follow.
    public Follow follow(Long followedCustomerId) {
        Customer followerCustomer = AuthHelper.getLoggedCustomer();

        // check if the followerCustomer can add more following
        if (followRepository.countFollowersFromCustomer(followerCustomer.getId()) >= MAX_FOLLOWS) {
            throw new FollowersLimitExceededException(Exceptions.FOLLOW.MAX_FOLLOWERS);
        }

        // check if the customer we want to add as a follow exists.
        Customer followedCustomer = customerRepository.findById(followedCustomerId).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        // check followedCustomer is not already following the followerCustomer
        if (followRepository.isFollowing(followedCustomer.getId(), followerCustomer.getId())) {
            throw new FollowAlreadyExistsException(Exceptions.FOLLOW.ALREADY_EXISTS);
        }

        // check followerCustomer and followedCustomer are not the same customer
        if (followerCustomer.getId().equals(followedCustomer.getId())) {
            throw new FollowYourselfNotAllowedException(Exceptions.FOLLOW.SELF_FOLLOW);
        }

        return followRepository.save(
                new Follow(followedCustomer, followerCustomer)
        );
    }

    // unfollow customer following by logged customer
    public void unfollow(Long customerId) {
        // check if the follow exists
        Follow follow = this.getFollow(customerId);

        followRepository.deleteById(follow.getId());
    }
}
