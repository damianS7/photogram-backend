package com.damian.photogram.posts.post;


import com.damian.photogram.posts.post.dto.PostDTO;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

public class PostDTOMapper {
    public static PostDTO map(Post post) {
        return new PostDTO(
                post.getId(),
                post.getCustomer().getId(),
                post.getDescription(),
                post.getPhotoFilename(),
                //                generatePhotoUrl(post.getPhotoFilename()),
                post.getCreatedAt().toString()
        );
    }

    public static Set<PostDTO> map(Set<Post> posts) {
        return posts
                .stream()
                .map(
                        PostDTOMapper::map
                ).collect(Collectors.toSet());
    }

    public static Page<PostDTO> map(Page<Post> posts) {
        return posts
                .map(
                        PostDTOMapper::map
                );
    }
}
