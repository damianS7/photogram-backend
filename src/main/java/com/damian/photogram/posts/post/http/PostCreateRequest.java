package com.damian.photogram.posts.post.http;

public record PostCreateRequest(
        String photoFilename,
        String description
) {
}
