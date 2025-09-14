package com.damian.photogram.domain.follow.dto;

import com.damian.photogram.domain.follow.Follow;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

public class FollowDtoMapper {
    public static FollowDto toFollowDto(Follow follow) {
        return new FollowDto(
                follow.getFollowedCustomer().getId(),
                follow.getFollowedCustomer().getProfile().getUsername(),
                follow.getFollowedCustomer().getProfile().getImageFilename(),
                follow.getFollowerCustomer().getId(),
                follow.getFollowerCustomer().getProfile().getUsername(),
                follow.getFollowerCustomer().getProfile().getImageFilename()
        );
    }

    public static Set<FollowDto> toFollowDtoSet(Set<Follow> follows) {
        return follows
                .stream()
                .map(
                        FollowDtoMapper::toFollowDto
                ).collect(Collectors.toSet());
    }

    public static Page<FollowDto> toFollowDtoPaged(Page<Follow> follows) {
        return follows
                .map(
                        FollowDtoMapper::toFollowDto
                );
    }
}
