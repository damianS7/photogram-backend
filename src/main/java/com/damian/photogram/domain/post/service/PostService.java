package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.post.dto.response.PostCreateRequest;
import com.damian.photogram.domain.post.exception.PostNotAuthorException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.helper.PostHelper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class PostService {
    private final PostRepository postRepository;
    private final CustomerRepository customerRepository;
    private final ProfileRepository profileRepository;
    private final ImageUploaderService imageUploaderService;

    public PostService(
            PostRepository postRepository,
            CustomerRepository customerRepository,
            ProfileRepository profileRepository,
            ImageUploaderService imageUploaderService
    ) {
        this.postRepository = postRepository;
        this.customerRepository = customerRepository;
        this.profileRepository = profileRepository;
        this.imageUploaderService = imageUploaderService;
    }

    public Page<Post> getPostsByUsername(String username, Pageable pageable) {
        // check if the customer exists by this username
        profileRepository.findByUsername(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        return postRepository.findAllByUsername(username, pageable);
    }

    // add a new post for the logged customer
    public Post createPost(PostCreateRequest request) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the customer we want to add as a post exists.
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

    // delete a post created by logged customer.
    public void deletePost(Long id) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Post post = postRepository.findById(id).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the logged customer is the owner of the post.
        if (!PostHelper.isAuthor(loggedCustomer, post)) {
            throw new PostNotAuthorException(Exceptions.POSTS.NOT_AUTHOR);
        }

        postRepository.deleteById(id);
        String path = PostHelper.getPostsImageUploadPath(post.getAuthor().getId());
        imageUploaderService.deleteImage(path, post.getPhotoFilename());
    }
}
