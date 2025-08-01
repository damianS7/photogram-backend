package com.damian.photogram.follower;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerRepository;
import com.damian.photogram.customer.exception.CustomerNotFoundException;
import com.damian.photogram.follower.exception.FollowerAlreadyExistException;
import com.damian.photogram.follower.exception.FollowerAuthorizationException;
import com.damian.photogram.follower.exception.FollowerNotFoundException;
import com.damian.photogram.follower.exception.MaxFollowersLimitReachedException;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FollowerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private FollowerService followerService;

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
    @DisplayName("Should get all friends")
    void shouldGetAllFriends() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer friend1 = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

        Customer friend2 = new Customer(
                3L, "customer2@test.com", passwordEncoder.encode("password")
        );

        Set<Follower> followerList = Set.of(
                new Follower(loggedCustomer, friend1),
                new Follower(loggedCustomer, friend2)
        );

        // when
        when(followerRepository.findAllByCustomerId(loggedCustomer.getId()))
                .thenReturn(followerList);
        Set<Follower> result = followerService.getFriends();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(followerRepository, times(1)).findAllByCustomerId(loggedCustomer.getId());
    }

    @Test
    @DisplayName("Should add a follower")
    void shouldAddFriend() {
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

        Follower givenFollower = new Follower(loggedCustomer, friendCustomer);

        // when
        when(customerRepository.findById(friendCustomer.getId())).thenReturn(Optional.of(friendCustomer));
        when(followerRepository.save(any(Follower.class)))
                .thenReturn(givenFollower);

        Follower result = followerService.addFriend(friendCustomer.getId());

        // then
        assertNotNull(result);
        verify(followerRepository, times(1)).save(any(Follower.class));
    }

    @Test
    @DisplayName("Should not add a follower when limit reached")
    void shouldNotAddFriendWhenLimitReached() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );

        setUpContext(loggedCustomer);
        short MAX_FRIENDS = 3;

        Field field = null;
        try {
            field = FollowerService.class.getDeclaredField("MAX_FRIENDS");
            field.setAccessible(true);
            MAX_FRIENDS = (short) field.get(followerService); // null porque es static
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }


        Set<Follower> followerList = new HashSet<>();
        for (int i = 0; i <= MAX_FRIENDS; i++) {
            followerList.add(new Follower());
        }

        // when
        when(followerRepository.findAllByCustomerId(loggedCustomer.getId())).thenReturn(followerList);
        MaxFollowersLimitReachedException exception = assertThrows(
                MaxFollowersLimitReachedException.class,
                () -> followerService.addFriend(0L)
        );

        // then
        assertEquals(Exceptions.FRIEND_LIST.MAX_FRIENDS, exception.getMessage());
    }

    @Test
    @DisplayName("Should not add a follower when already exists")
    void shouldNotAddFriendWhenAlreadyExists() {
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
        when(followerRepository.friendExists(loggedCustomer.getId(), friend1.getId())).thenReturn(true);
        FollowerAlreadyExistException exception = assertThrows(
                FollowerAlreadyExistException.class,
                () -> followerService.addFriend(friend1.getId())
        );

        // then
        assertEquals(Exceptions.FRIEND_LIST.ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    @DisplayName("Should not add a follower when customer not found")
    void shouldNotAddFriendWhenCustomerNotFound() {
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
                () -> followerService.addFriend(friend1.getId())
        );

        // then
        assertEquals(Exceptions.CUSTOMER.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should delete a follower")
    void shouldDeleteFriend() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer friend1 = new Customer(
                2L, "customer1@test.com", passwordEncoder.encode("password")
        );

        Follower givenCC = new Follower(loggedCustomer, friend1);
        givenCC.setId(1L);

        // when
        when(followerRepository.findById(givenCC.getId())).thenReturn(Optional.of(givenCC));
        doNothing().when(followerRepository).deleteById(givenCC.getId());

        followerService.deleteFriend(givenCC.getId());

        // then
        verify(followerRepository, times(1)).deleteById(givenCC.getId());
    }

    @Test
    @DisplayName("Should not delete a follower when not found")
    void shouldNotDeleteFriendWhenNotFound() {
        // given
        Customer loggedCustomer = new Customer(1L, "customer@test.com", passwordEncoder.encode("password"));
        setUpContext(loggedCustomer);

        // when
        when(followerRepository.findById(anyLong())).thenReturn(Optional.empty());
        FollowerNotFoundException exception = assertThrows(
                FollowerNotFoundException.class,
                () -> followerService.deleteFriend(0L)
        );

        // then
        assertEquals(Exceptions.FRIEND_LIST.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should not delete a follower when not authorized")
    void shouldNotDeleteFriendWhenNotAuthorized() {
        // given
        Customer loggedCustomer = new Customer(1L, "customer@test.com", passwordEncoder.encode("password"));
        setUpContext(loggedCustomer);

        Follower givenCC = new Follower(
                new Customer(5L, "customer1@test.com", passwordEncoder.encode("password")),
                new Customer(8L, "customer2@test.com", passwordEncoder.encode("password"))
        );
        givenCC.setId(1L);

        // when
        when(followerRepository.findById(givenCC.getId())).thenReturn(Optional.of(givenCC));
        FollowerAuthorizationException exception = assertThrows(
                FollowerAuthorizationException.class,
                () -> followerService.deleteFriend(givenCC.getId())
        );

        // then
        assertEquals(Exceptions.FRIEND_LIST.ACCESS_FORBIDDEN, exception.getMessage());
    }

}
