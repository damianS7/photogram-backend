package com.damian.photogram.domain.post.helper;

import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.model.Post;

public class PostHelper {

    public static boolean isAuthor(Customer customer, Post post) {
        // check if the customer is the author of the post.
        return customer.getId().equals(post.getCustomer().getId());
    }
}
