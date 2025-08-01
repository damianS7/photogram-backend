package com.damian.photogram.follower;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerRepository;
import com.damian.photogram.customer.exception.CustomerNotFoundException;
import com.damian.photogram.follower.exception.FollowerAlreadyExistException;
import com.damian.photogram.follower.exception.FollowerAuthorizationException;
import com.damian.photogram.follower.exception.FollowerNotFoundException;
import com.damian.photogram.follower.exception.MaxFollowersLimitReachedException;
import org.springframework.stereotype.Service;

import java.util.Set;


@Service
public class FollowerService {
    private final short MAX_FRIENDS = 10;
    private final FollowerRepository followerRepository;
    private final CustomerRepository customerRepository;

    public FollowerService(
            FollowerRepository followerRepository,
            CustomerRepository customerRepository
    ) {
        this.followerRepository = followerRepository;
        this.customerRepository = customerRepository;
    }

    // get all the friends for the logged customer
    public Set<Follower> getFriends() {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();
        return followerRepository.findAllByCustomerId(loggedCustomer.getId());
    }

    // add a new follower for the logged customer
    public Follower addFriend(Long customerId) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check follower list size limit
        if (this.getFriends().size() >= MAX_FRIENDS) {
            throw new MaxFollowersLimitReachedException(Exceptions.FRIEND_LIST.MAX_FRIENDS);
        }

        // check if the customer we want to add as a follower exists.
        Customer friendCustomer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        // check if that they are not already follower
        if (followerRepository.friendExists(loggedCustomer.getId(), friendCustomer.getId())) {
            throw new FollowerAlreadyExistException(Exceptions.FRIEND_LIST.ALREADY_EXISTS);
        }

        // check if the logged customer is not the follower
        if (loggedCustomer.getId().equals(friendCustomer.getId())) {
            throw new FollowerAuthorizationException(Exceptions.FRIEND_LIST.ACCESS_FORBIDDEN);
        }

        return followerRepository.save(
                new Follower(loggedCustomer, friendCustomer)
        );
    }

    // delete a follower from the follower list of the logged customer.
    public void deleteFriend(Long id) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the follower exists
        Follower follower = followerRepository.findById(id).orElseThrow(
                () -> new FollowerNotFoundException(Exceptions.FRIEND_LIST.NOT_FOUND)
        );

        // check if the logged customer is the owner of the follower.
        if (!loggedCustomer.getId().equals(follower.getCustomer().getId())) {
            throw new FollowerAuthorizationException(Exceptions.FRIEND_LIST.ACCESS_FORBIDDEN);
        }

        followerRepository.deleteById(id);
    }
}
