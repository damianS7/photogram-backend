package com.damian.photogram.web.rest.user.dto.mapper;

import com.damian.photogram.web.rest.user.dto.response.FollowDto;
import com.damian.photogram.domain.user.model.Follow;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

public class FollowDtoMapper {
    public static FollowDto toFollowDto(Follow follow) {
        return new FollowDto(
                follow.getFollowerCustomer().getId(),
                follow.getFollowerCustomer().getProfile().getUsername(),
                follow.getFollowerCustomer().getProfile().getImageFilename(),
                follow.getFollowedCustomer().getId(),
                follow.getFollowedCustomer().getProfile().getUsername(),
                follow.getFollowedCustomer().getProfile().getImageFilename()
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
