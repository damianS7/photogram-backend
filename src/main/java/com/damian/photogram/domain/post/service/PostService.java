package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageStorageService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.post.dto.request.PostCreateRequest;
import com.damian.photogram.domain.post.exception.PostNotAuthorException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.helper.PostHelper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class PostService {
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final ImageStorageService imageStorageService;

    public PostService(
            PostRepository postRepository,
            ProfileRepository profileRepository,
            ImageStorageService imageStorageService
    ) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * Get posts paged by username
     *
     * @param username the username to get posts from
     * @param pageable pagination parameters
     * @return Page<Post>
     */
    public Page<Post> getPostsByUsername(String username, Pageable pageable) {
        // check if the customer exists by this username
        profileRepository.findByUsername(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        return postRepository.findAllByUsername(username, pageable);
    }

    /**
     * Add a new post for the current customer
     *
     * @param request
     * @return Post the post created
     */
    public Post createPost(PostCreateRequest request) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // create the post
        Post post = Post.create(currentCustomer)
                        .setPhotoFilename(request.photoFilename())
                        .setDescription(request.description());

        // save the post
        return postRepository.save(
                post
        );
    }

    /**
     * Delete a post created given its id.
     * You can only delete your own posts.
     *
     * @param id the id of the post to be deleted.
     * @throws PostNotFoundException  if the post does not exist.
     * @throws PostNotAuthorException if the current customer is not the author of the post.
     */
    public void deletePost(Long id) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Post post = postRepository.findById(id).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the current customer is the owner of the post.
        if (!post.isAuthor(currentCustomer)) {
            throw new PostNotAuthorException(Exceptions.POSTS.NOT_AUTHOR);
        }

        // path to the folder where the image is stored.
        String path = PostHelper.getPostsImagePath(post.getAuthor().getId());

        // delete the image from the storage.
        imageStorageService.deleteImage(path, post.getPhotoFilename());

        // delete the post from the database.
        postRepository.deleteById(id);
    }
}
