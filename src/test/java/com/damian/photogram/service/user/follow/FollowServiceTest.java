package com.damian.photogram.service.user.follow;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.exception.FollowAlreadyExistsException;
import com.damian.photogram.domain.user.exception.FollowNotFoundException;
import com.damian.photogram.domain.user.exception.FollowersLimitExceededException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Follow;
import com.damian.photogram.domain.user.repository.CustomerRepository;
import com.damian.photogram.domain.user.repository.FollowRepository;
import com.damian.photogram.service.user.FollowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FollowServiceTest extends AbstractServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowService followService;

    @Test
    @DisplayName("Should get followers paginated")
    void shouldGetFollowersPaginated() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@test.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        setUpContext(currentCustomer);

        Customer follow1 = Customer.create()
                                   .setId(2L)
                                   .setEmail("customer2@test.com")
                                   .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Customer follow2 = Customer.create()
                                   .setId(3L)
                                   .setEmail("customer3@test.com")
                                   .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Set<Follow> followerList = Set.of(
                Follow.create().setFollowedCustomer(currentCustomer).setFollowerCustomer(follow1),
                Follow.create().setFollowedCustomer(currentCustomer).setFollowerCustomer(follow2)
        );

        Page<Follow> followPage = new PageImpl<>(followerList.stream().toList());
        Pageable pageable = PageRequest.of(0, followerList.size());

        // when
        when(customerRepository.existsById(currentCustomer.getId())).thenReturn(true);
        when(followRepository.findAllByFollowedCustomer_Id(currentCustomer.getId(), pageable))
                .thenReturn(followPage);

        Page<Follow> result = followService.getFollowers(pageable);

        // then
        assertNotNull(result);
        assertEquals(followerList.size(), result.getSize());
        verify(followRepository, times(1)).findAllByFollowedCustomer_Id(currentCustomer.getId(), pageable);
    }

    @Test
    @DisplayName("Should get followings paginated")
    void shouldGetFollowingsPaginated() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Customer follow1 = Customer.create()
                                   .setId(2L)
                                   .setEmail("customer2@test.com")
                                   .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Customer follow2 = Customer.create()
                                   .setId(3L)
                                   .setEmail("customer3@test.com")
                                   .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Set<Follow> followingList = Set.of(
                new Follow(follow1, currentCustomer),
                new Follow(follow2, currentCustomer)
        );

        Page<Follow> followPage = new PageImpl<>(followingList.stream().toList());
        Pageable pageable = PageRequest.of(0, 2);

        // when
        when(customerRepository.existsById(currentCustomer.getId())).thenReturn(true);
        when(followRepository.findAllByFollowerCustomer_Id(currentCustomer.getId(), pageable))
                .thenReturn(followPage);
        Page<Follow> result = followService.getFollowing(pageable);

        // then
        assertNotNull(result);
        assertEquals(followingList.size(), result.getSize());
        verify(followRepository, times(1)).findAllByFollowerCustomer_Id(currentCustomer.getId(), pageable);
    }

    @Test
    @DisplayName("Should follow a user")
    void shouldFollow() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@test.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD));
        setUpContext(currentCustomer);

        Customer friendCustomer = Customer.create()
                                          .setId(2L)
                                          .setEmail("customer2@test.com")
                                          .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Follow givenFollow = Follow
                .create()
                .follower(currentCustomer)
                .follows(friendCustomer);

        // when
        when(customerRepository.findById(friendCustomer.getId())).thenReturn(Optional.of(friendCustomer));
        when(followRepository.save(any(Follow.class)))
                .thenReturn(givenFollow);

        Follow result = followService.follow(friendCustomer.getId());

        // then
        assertNotNull(result);
        assertEquals(currentCustomer.getId(), result.getFollowerCustomer().getId());
        assertEquals(friendCustomer.getId(), result.getFollowedCustomer().getId());
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    @DisplayName("Should not add a follow when limit reached")
    void shouldNotFollowWhenLimitReached() {
        // given
        Customer followerCustomer = Customer.create()
                                            .setId(1L)
                                            .setEmail("customer@test.com")
                                            .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        setUpContext(followerCustomer);
        int MAX_FOLLOWS = 0;

        Field field = null;
        try {
            field = FollowService.class.getDeclaredField("MAX_FOLLOWS");
            field.setAccessible(true);
            MAX_FOLLOWS = (int) field.get(followService); // null porque es static
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // when
        when(followRepository.countFollowers(followerCustomer.getId())).thenReturn((long) MAX_FOLLOWS + 1);
        FollowersLimitExceededException exception = assertThrows(
                FollowersLimitExceededException.class,
                () -> followService.follow(0L)
        );

        // then
        assertEquals(Exceptions.FOLLOW.MAX_FOLLOWERS, exception.getMessage());
    }

    @Test
    @DisplayName("Should not add a follow when already exists")
    void shouldNotFollowWhenAlreadyExists() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@test.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD));
        setUpContext(currentCustomer);

        Customer friend1 = Customer.create()
                                   .setId(2L)
                                   .setEmail("customer2@test.com")
                                   .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // when
        when(customerRepository.findById(friend1.getId())).thenReturn(Optional.of(friend1));
        when(followRepository.isFollowing(anyLong(), anyLong())).thenReturn(true);
        FollowAlreadyExistsException exception = assertThrows(
                FollowAlreadyExistsException.class,
                () -> followService.follow(friend1.getId())
        );

        // then
        assertEquals(Exceptions.FOLLOW.ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    @DisplayName("Should not add a follow when customer not found")
    void shouldNotFollowWhenCustomerNotFound() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@test.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        setUpContext(currentCustomer);

        Customer friend1 = Customer.create()
                                   .setId(2L)
                                   .setEmail("customer2@test.com")
                                   .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // when
        when(customerRepository.findById(friend1.getId())).thenReturn(Optional.empty());
        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> followService.follow(friend1.getId())
        );

        // then
        assertEquals(Exceptions.CUSTOMER.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should unfollow")
    void shouldUnfollow() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@test.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        setUpContext(currentCustomer);

        Customer followedCustomer = Customer.create()
                                            .setId(2L)
                                            .setEmail("customer2@test.com")
                                            .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        Follow givenFollow = new Follow(followedCustomer, currentCustomer);
        givenFollow.setId(1L);

        // when
        when(customerRepository.existsById(followedCustomer.getId())).thenReturn(true);
        when(followRepository.findFollowRelationshipBetweenCustomers(currentCustomer.getId(), followedCustomer.getId()))
                .thenReturn(Optional.of(givenFollow));
        doNothing().when(followRepository).deleteById(givenFollow.getId());

        followService.unfollow(followedCustomer.getId());

        // then
        verify(followRepository, times(1)).deleteById(givenFollow.getId());
    }

    @Test
    @DisplayName("Should not delete a follow when not found")
    void shouldNotUnfollowWhenNotFound() {
        // given
        Customer currentCustomer = Customer.create()
                                           .setId(1L)
                                           .setEmail("customer@test.com")
                                           .setPassword(passwordEncoder.encode(RAW_PASSWORD));
        setUpContext(currentCustomer);

        Customer followedCustomer = Customer.create()
                                            .setId(2L)
                                            .setEmail("customer2@test.com")
                                            .setPassword(passwordEncoder.encode(RAW_PASSWORD));

        // when
        when(customerRepository.existsById(followedCustomer.getId())).thenReturn(true);
        when(followRepository.findFollowRelationshipBetweenCustomers(
                anyLong(),
                anyLong()
        )).thenReturn(Optional.empty());
        FollowNotFoundException exception = assertThrows(
                FollowNotFoundException.class,
                () -> followService.unfollow(followedCustomer.getId())
        );

        // then
        assertEquals(Exceptions.FOLLOW.NOT_FOUND, exception.getMessage());
    }
}
