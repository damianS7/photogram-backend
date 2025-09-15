package com.damian.photogram.app.feed;

import com.damian.photogram.app.feed.dto.response.FeedDto;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.follow.FollowRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.customer.exception.ProfileNotFoundException;
import com.damian.photogram.domain.user.customer.model.Profile;
import com.damian.photogram.domain.user.customer.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Service class responsible for managing user feeds.
 */
@Service
public class FeedService {
    private static final Logger log = LoggerFactory.getLogger(FeedService.class);
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final FollowRepository followRepository;

    public FeedService(
            PostRepository postRepository,
            ProfileRepository profileRepository,
            FollowRepository followRepository
    ) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.followRepository = followRepository;
    }

    /**
     * Retrieves the feed data for a specific user.
     *
     * @param username The username of the user whose feed is to be retrieved.
     * @return A FeedDto containing the user's feed data.
     * @throws ProfileNotFoundException if the user with the given username is not found.
     */
    public FeedDto getUserFeed(String username) {
        final Profile profile = profileRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> {
                    log.error("Failed to fetch profile for username: {}", username);
                    return new ProfileNotFoundException(Exceptions.FEED.USER_PROFILE_NOT_FOUND);
                }
        );

        return new FeedDto(
                profile.getOwner().getId(),
                profile.getUsername(),
                postRepository.countByAuthorId(profile.getOwner().getId()),
                followRepository.countFollowsFromCustomer(profile.getOwner().getId()),
                followRepository.countFollowersFromCustomer(profile.getOwner().getId()),
                profile.getImageFilename(),
                profile.getAboutMe()
        );
    }
}
