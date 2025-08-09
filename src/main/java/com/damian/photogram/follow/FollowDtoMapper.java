package com.damian.photogram.follow;

import com.damian.photogram.follow.dto.FollowDto;

import java.util.Set;
import java.util.stream.Collectors;

public class FollowDtoMapper {
    public static FollowDto toDto(Follow follow) {
        return new FollowDto(
                follow.getId(),
                follow.getFollowedCustomer().getId(),
                follow.getFollowedCustomer().getUsername(),
                follow.getFollowerCustomer().getId(),
                follow.getFollowerCustomer().getUsername(),
                follow.getFollowedCustomer().getProfile().getImageFilename()
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
