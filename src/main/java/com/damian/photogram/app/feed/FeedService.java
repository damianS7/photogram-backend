package com.damian.photogram.app.feed;

import com.damian.photogram.app.feed.dto.response.FeedDto;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.follow.FollowRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.customer.model.Profile;
import com.damian.photogram.domain.user.customer.repository.ProfileRepository;
import org.springframework.stereotype.Service;


@Service
public class FeedService {
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

    public FeedDto getUserFeed(String username) {
        final Profile profile = profileRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND, null)
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
