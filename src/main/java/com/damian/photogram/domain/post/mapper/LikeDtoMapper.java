package com.damian.photogram.domain.post.mapper;

import com.damian.photogram.domain.post.dto.response.LikeDto;
import com.damian.photogram.domain.post.dto.response.PostLikeDataDto;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;

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

    public static PostLikeDataDto toPostLikeDto(Post post) {
        return new PostLikeDataDto(
                post.getId(),
                true,
                100L
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
