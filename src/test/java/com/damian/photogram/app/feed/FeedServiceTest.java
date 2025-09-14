package com.damian.photogram.app.feed;

import com.damian.photogram.app.feed.dto.response.FeedDto;
import com.damian.photogram.domain.follow.FollowRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.user.customer.repository.ProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Test
    @DisplayName("Should get feed")
    void shouldGetFeed() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        loggedCustomer.getProfile().setUsername("customer7777");

        // when
        when(profileRepository.findByUsernameIgnoreCase(loggedCustomer.getUsername()))
                .thenReturn(Optional.of(loggedCustomer.getProfile()));

        when(postRepository.countByAuthorId(anyLong()))
                .thenReturn(0L);

        when(followRepository.countFollowsFromCustomer(anyLong()))
                .thenReturn(0L);

        when(followRepository.countFollowersFromCustomer(anyLong()))
                .thenReturn(0L);
        FeedDto result = feedService.getUserFeed(loggedCustomer.getUsername());

        // then
        assertThat(result)
                .isNotNull();
        verify(profileRepository, times(1)).findByUsernameIgnoreCase(loggedCustomer.getUsername());
    }
}
