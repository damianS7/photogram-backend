package com.damian.photogram.domain.customer;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.exception.FollowAlreadyExistsException;
import com.damian.photogram.domain.customer.exception.FollowNotFoundException;
import com.damian.photogram.domain.customer.exception.FollowersLimitExceededException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Follow;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.repository.FollowRepository;
import com.damian.photogram.domain.customer.service.FollowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private FollowService followService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        customerRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should get all follows")
    void shouldGetAllFollows() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer follow1 = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

        Customer follow2 = new Customer(
                3L, "customer2@test.com", passwordEncoder.encode("password")
        );

        Set<Follow> followList = Set.of(
                new Follow(loggedCustomer, follow1),
                new Follow(loggedCustomer, follow2)
        );

        // when
        when(customerRepository.existsById(loggedCustomer.getId())).thenReturn(true);
        when(followRepository.findAllByFollowedCustomer_Id(loggedCustomer.getId()))
                .thenReturn(followList);
        Set<Follow> result = followService.getFollowers();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(followRepository, times(1)).findAllByFollowedCustomer_Id(loggedCustomer.getId());
    }

    @Test
    @DisplayName("Should add a follow")
    void shouldFollow() {
        // given
        Customer loggedCustomer = new Customer(
                1L,
                "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer friendCustomer = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

        Follow givenFollow = new Follow(loggedCustomer, friendCustomer);

        // when
        when(customerRepository.findById(friendCustomer.getId())).thenReturn(Optional.of(friendCustomer));
        when(followRepository.save(any(Follow.class)))
                .thenReturn(givenFollow);

        Follow result = followService.follow(friendCustomer.getId());

        // then
        assertNotNull(result);
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    @DisplayName("Should not add a follow when limit reached")
    void shouldNotFollowWhenLimitReached() {
        // given
        Customer followerCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );

        setUpContext(followerCustomer);
        short MAX_FOLLOWS = 3;

        Field field = null;
        try {
            field = FollowService.class.getDeclaredField("MAX_FOLLOWS");
            field.setAccessible(true);
            MAX_FOLLOWS = (short) field.get(followService); // null porque es static
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // when
        when(followRepository.countFollowersFromCustomer(followerCustomer.getId())).thenReturn((long) MAX_FOLLOWS + 1);
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
        Customer loggedCustomer = new Customer(
                1L,
                "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer friend1 = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

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
        Customer loggedCustomer = new Customer(
                1L,
                "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer friend1 = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

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
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer customerToFollow = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

        Follow givenFollow = new Follow(loggedCustomer, customerToFollow);
        givenFollow.setId(1L);

        // when
        when(followRepository.findFollowRelationshipBetweenCustomers(givenFollow.getId(), loggedCustomer.getId()))
                .thenReturn(Optional.of(givenFollow));
        doNothing().when(followRepository).deleteById(givenFollow.getId());

        followService.unfollow(givenFollow.getId());

        // then
        verify(followRepository, times(1)).deleteById(givenFollow.getId());
    }

    @Test
    @DisplayName("Should not delete a follow when not found")
    void shouldNotUnfollowWhenNotFound() {
        // given
        Customer loggedCustomer = new Customer(1L, "customer@test.com", passwordEncoder.encode("password"));
        setUpContext(loggedCustomer);

        // when
        when(followRepository.findFollowRelationshipBetweenCustomers(
                anyLong(),
                anyLong()
        )).thenReturn(Optional.empty());
        FollowNotFoundException exception = assertThrows(
                FollowNotFoundException.class,
                () -> followService.unfollow(0L)
        );

        // then
        assertEquals(Exceptions.FOLLOW.NOT_FOUND, exception.getMessage());
    }
}
