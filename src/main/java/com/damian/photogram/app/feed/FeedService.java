package com.damian.photogram.app.feed;

import com.damian.photogram.app.feed.dto.response.FeedDto;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.FollowRepository;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
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
        final Profile profile = profileRepository.findByUsername(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
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
