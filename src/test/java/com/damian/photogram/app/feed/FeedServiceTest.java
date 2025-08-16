package com.damian.photogram.app.feed;

import com.damian.photogram.app.feed.dto.FeedDto;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.FollowRepository;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {

    @Mock
    private ProfileRepository profileRepository;


    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;


    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private FeedService feedService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should get feed")
    void shouldGetFeed() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        loggedCustomer.getProfile().setUsername("customer7777");
        //        setUpContext(loggedCustomer);

        // when
        when(profileRepository.findByUsername(loggedCustomer.getUsername()))
                .thenReturn(Optional.of(loggedCustomer.getProfile()));

        when(postRepository.countByCustomerId(anyLong()))
                .thenReturn(0L);

        when(followRepository.countFollowsFromCustomer(anyLong()))
                .thenReturn(0L);

        when(followRepository.countFollowersFromCustomer(anyLong()))
                .thenReturn(0L);
        FeedDto result = feedService.getUserFeed(loggedCustomer.getUsername());

        // then
        assertThat(result)
                .isNotNull();
        verify(profileRepository, times(1)).findByUsername(loggedCustomer.getUsername());
    }
}
