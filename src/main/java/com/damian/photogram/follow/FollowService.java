package com.damian.photogram.follow;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerRepository;
import com.damian.photogram.customer.exception.CustomerNotFoundException;
import com.damian.photogram.follow.exception.*;
import org.springframework.stereotype.Service;

import java.util.Set;


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
    public Set<Follow> getFollowers() {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return getFollowers(loggedCustomer.getId());
    }

    // get all the followers from a specific customer
    public Set<Follow> getFollowers(Long customerId) {
        return followRepository.findAllByFollowedCustomer_Id(customerId);
    }

    // get all the followed users by the logged customer
    public Set<Follow> getFollowed() {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return getFollowed(loggedCustomer.getId());
    }

    // get all the followed users from a specific customer
    public Set<Follow> getFollowed(Long customerId) {
        return followRepository.findAllByFollowerCustomer_Id(customerId);
    }

    // followedCustomerId is the id of the customer the logged customer will follow.
    public Follow follow(Long followedCustomerId) {
        Customer followerCustomer = AuthHelper.getLoggedCustomer();

        // check if the followerCustomer can add more followed
        if (followRepository.countByFollowedCustomer_Id(followerCustomer.getId()) >= MAX_FOLLOWS) {
            throw new FollowersLimitExceededException(Exceptions.FOLLOW.MAX_FOLLOWERS);
        }

        // check if the customer we want to add as a follow exists.
        Customer followedCustomer = customerRepository.findById(followedCustomerId).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        // check followedCustomer is not already following the followerCustomer
        if (followRepository.isFollowing(followerCustomer.getId(), followedCustomer.getId())) {
            throw new FollowerAlreadyExistsException(Exceptions.FOLLOW.ALREADY_EXISTS);
        }

        // check followerCustomer and followedCustomer are not the same customer
        if (followerCustomer.getId().equals(followedCustomer.getId())) {
            throw new CannotFollowYourselfException(Exceptions.FOLLOW.SELF_FOLLOW);
        }

        return followRepository.save(
                new Follow(followerCustomer, followedCustomer)
        );
    }

    // TODO: ...
    // removes the follow .
    // removes the follow for the logged customer.
    public void unfollow(Long id) {
        Customer followedCustomer = AuthHelper.getLoggedCustomer();

        // check if the follow exists
        Follow follow = followRepository.findById(id).orElseThrow(
                () -> new FollowNotFoundException(Exceptions.FOLLOW.NOT_FOUND)
        );

        // check if the logged customer is the owner of the follow.
        if (!followedCustomer.getId().equals(follow.getFollowedCustomer().getId())) {
            throw new FollowerAuthorizationException(Exceptions.FOLLOW.ACCESS_FORBIDDEN);
        }

        followRepository.deleteById(id);
    }
}
