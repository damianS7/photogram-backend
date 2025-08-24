package com.damian.photogram.domain.post.helper;

public class PostHelper {
    private static final String POSTS_IMAGE_UPLOAD_PATH = "customers/{id}/posts/";

    public static String getPostsImagePath(Long customerId) {
        return POSTS_IMAGE_UPLOAD_PATH.replace("{id}", customerId.toString());
    }
}
