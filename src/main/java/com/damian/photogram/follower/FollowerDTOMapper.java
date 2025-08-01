package com.damian.photogram.follower;

import com.damian.photogram.follower.dto.FollowerDTO;

import java.util.Set;
import java.util.stream.Collectors;

public class FollowerDTOMapper {
    public static FollowerDTO toCustomerFriendDTO(Follower follower) {
        return new FollowerDTO(
                follower.getId(),
                follower.getFriend().getId(),
                follower.getFriend().getFullName(),
                follower.getFriend().getProfile().getAvatarFilename()
        );
    }

    public static Set<FollowerDTO> toFriendDTOList(Set<Follower> customers) {
        return customers
                .stream()
                .map(
                        FollowerDTOMapper::toCustomerFriendDTO
                ).collect(Collectors.toSet());
    }
}
