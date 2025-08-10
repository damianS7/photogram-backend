package com.damian.photogram.follow;

import com.damian.photogram.follow.dto.FollowDto;

import java.util.Set;
import java.util.stream.Collectors;

public class FollowDtoMapper {
    public static FollowDto toDto(Follow follow) {
        return new FollowDto(
                follow.getFollowedCustomer().getId(),
                follow.getFollowedCustomer().getProfile().getUsername(),
                follow.getFollowedCustomer().getProfile().getImageFilename(),
                follow.getFollowerCustomer().getId(),
                follow.getFollowerCustomer().getProfile().getUsername(),
                follow.getFollowerCustomer().getProfile().getImageFilename()
        );
    }

    public static Set<FollowDto> toDtoSet(Set<Follow> follows) {
        return follows
                .stream()
                .map(
                        FollowDtoMapper::toDto
                ).collect(Collectors.toSet());
    }
}
