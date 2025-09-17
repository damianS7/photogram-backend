package com.damian.photogram.web.rest.post.dto.mapper;

import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.web.rest.post.dto.response.LikeDto;

import java.util.Set;
import java.util.stream.Collectors;

public class LikeDtoMapper {
    public static LikeDto toLikeDto(Like like) {
        return new LikeDto(
                like.getId(),
                like.getPost().getId(),
                like.getCustomer().getId()
        );
    }

    public static Set<LikeDto> toLikeDtoSet(Set<Like> likes) {
        return likes
                .stream()
                .map(
                        LikeDtoMapper::toLikeDto
                ).collect(Collectors.toSet());
    }
}
