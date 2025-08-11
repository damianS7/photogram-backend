package com.damian.photogram.posts.post;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.customers.profile.ProfileRepository;
import com.damian.photogram.posts.post.exception.PostAuthorizationException;
import com.damian.photogram.posts.post.exception.PostNotFoundException;
import com.damian.photogram.posts.post.http.PostCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class PostService {
    private final PostRepository postRepository;
    private final CustomerRepository customerRepository;
    private final ProfileRepository profileRepository;

    public PostService(
            PostRepository postRepository,
            CustomerRepository customerRepository,
            ProfileRepository profileRepository
    ) {
        this.postRepository = postRepository;
        this.customerRepository = customerRepository;
        this.profileRepository = profileRepository;
    }

    public Page<Post> getPostsByUsername(String username, Pageable pageable) {
        // check if the customers exists by this username
        profileRepository.findByUsername(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        return postRepository.findAllByUsername(username, pageable);
    }

    // add a new post for the logged customers
    public Post addPost(PostCreateRequest request) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the customers we want to add as a post exists.
        Customer customer = customerRepository.findById(loggedCustomer.getId()).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        Post post = new Post(customer);
        post.setPhotoFilename(request.photoFilename());
        post.setDescription(request.description());
        post.setCreatedAt(Instant.now());

        return postRepository.save(
                post
        );
    }

    // delete a post from the post list of the logged customers.
    public void deletePost(Long id) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Post post = postRepository.findById(id).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the logged customers is the owner of the post.
        if (!loggedCustomer.getId().equals(post.getCustomer().getId())) {
            throw new PostAuthorizationException(Exceptions.POSTS.ACCESS_FORBIDDEN);
        }

        postRepository.deleteById(id);
    }
}
