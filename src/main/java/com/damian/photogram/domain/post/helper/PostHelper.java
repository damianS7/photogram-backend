package com.damian.photogram.domain.post.helper;

import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.model.Post;

public class PostHelper {
    private static final String POSTS_IMAGE_UPLOAD_PATH = "customers/{id}/posts/";

    public static boolean isAuthor(Customer customer, Post post) {
        // check if the customer is the author of the post.
        return customer.getId().equals(post.getCustomer().getId());
    }

    public static String getPostsImageUploadPath(Long customerId) {
        return POSTS_IMAGE_UPLOAD_PATH.replace("{id}", customerId.toString());
    }
}
