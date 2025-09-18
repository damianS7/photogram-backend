package com.damian.photogram.service.feed;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.exception.ProfileNotFoundException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.FollowRepository;
import com.damian.photogram.domain.user.repository.ProfileRepository;
import com.damian.photogram.web.rest.feed.dto.response.FeedDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class FeedServiceTest extends AbstractServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FeedService feedService;

    @Test
    @DisplayName("Should get feed")
    void shouldGetFeed() {
        // given
        Customer givenCustomer = Customer.create()
                                         .setId(1L)
                                         .setEmail("customer@demo.com")
                                         .setPassword(passwordEncoder.encode(RAW_PASSWORD));
        givenCustomer.getProfile().setUsername("david");
        givenCustomer.getProfile().setAboutMe("Hello im david");
        givenCustomer.getProfile().setImageFilename("avatar.jpg");

        // when
        when(profileRepository.findByUsernameIgnoreCase(givenCustomer.getUsername()))
                .thenReturn(Optional.of(givenCustomer.getProfile()));

        when(postRepository.countPostsFromAuthor(anyLong()))
                .thenReturn(0L);

        when(followRepository.countFollowing(anyLong()))
                .thenReturn(0L);

        when(followRepository.countFollowers(anyLong()))
                .thenReturn(0L);

        FeedDto feedResult = feedService.getUserFeed(givenCustomer.getUsername());

        // then
        assertThat(feedResult)
                .isNotNull()
                .extracting(
                        FeedDto::customerId,
                        FeedDto::totalPosts,
                        FeedDto::followers,
                        FeedDto::following,
                        FeedDto::username,
                        FeedDto::aboutMe,
                        FeedDto::profileImageFilename
                ).containsExactly(
                        givenCustomer.getId(),
                        0L,
                        0L,
                        0L,
                        givenCustomer.getUsername(),
                        givenCustomer.getProfile().getAboutMe(),
                        givenCustomer.getProfile().getImageFilename()
                );

        verify(profileRepository, times(1)).findByUsernameIgnoreCase(givenCustomer.getUsername());
    }

    @Test
    @DisplayName("Should not get feed when profile not exists")
    void shouldNotGetFeedWhenProfileNotExists() {
        // given
        Customer givenCustomer = Customer.create()
                                         .setId(1L)
                                         .setEmail("customer@demo.com")
                                         .setPassword(passwordEncoder.encode(RAW_PASSWORD));
        givenCustomer.getProfile().setUsername("david");
        givenCustomer.getProfile().setAboutMe("Hello im david");
        givenCustomer.getProfile().setImageFilename("avatar.jpg");

        // when
        when(profileRepository.findByUsernameIgnoreCase(givenCustomer.getUsername()))
                .thenReturn(Optional.empty());

        ProfileNotFoundException exception = assertThrows(
                ProfileNotFoundException.class,
                () -> feedService.getUserFeed(givenCustomer.getUsername())
        );

        // then
        assertThat(exception)
                .isNotNull()
                .extracting(
                        ProfileNotFoundException::getMessage
                ).isEqualTo(Exceptions.FEED.USER_PROFILE_NOT_FOUND);
        verify(profileRepository, times(1)).findByUsernameIgnoreCase(givenCustomer.getUsername());
    }
}
