package com.damian.photogram.domain.post.mapper;


import com.damian.photogram.domain.post.dto.response.PostDto;
import com.damian.photogram.domain.post.model.Post;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

public class PostDtoMapper {
    public static PostDto toPostDtoPaginated(Post post) {
        return new PostDto(
                post.getId(),
                post.getAuthor().getId(),
                post.getDescription(),
                post.getPhotoFilename(),
                post.getCreatedAt().toString()
        );
    }

    public static Set<PostDto> toPostDtoSet(Set<Post> posts) {
        return posts
                .stream()
                .map(
                        PostDtoMapper::toPostDtoPaginated
                ).collect(Collectors.toSet());
    }

    public static Page<PostDto> toPostDtoPaginated(Page<Post> posts) {
        return posts
                .map(
                        PostDtoMapper::toPostDtoPaginated
                );
    }
}
