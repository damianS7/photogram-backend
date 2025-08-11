package com.damian.photogram.feed;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.profile.Profile;
import com.damian.photogram.customers.profile.ProfileRepository;
import com.damian.photogram.feed.dto.FeedDTO;
import com.damian.photogram.follow.FollowRepository;
import com.damian.photogram.posts.post.PostRepository;
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

    public FeedDTO getUserFeed(String username) {
        Profile profile = profileRepository.findByUsername(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        return new FeedDTO(
                profile.getOwner().getId(),
                username,
                postRepository.countByCustomerId(profile.getOwner().getId()),
                followRepository.countFollowsFromCustomer(profile.getOwner().getId()),
                followRepository.countFollowersFromCustomer(profile.getOwner().getId()),
                profile.getImageFilename(),
                profile.getAboutMe()
        );
    }


}
