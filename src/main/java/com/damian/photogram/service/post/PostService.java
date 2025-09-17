package com.damian.photogram.service.post;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.exception.PostOwnershipException;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.exception.CustomerNotFoundException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.repository.ProfileRepository;
import com.damian.photogram.web.rest.post.dto.request.PostCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * This class handles basic operations for a post like create, delete, and get posts.
 */
@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final PostImageService postImageService;

    public PostService(
            PostRepository postRepository,
            ProfileRepository profileRepository,
            PostImageService postImageService
    ) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.postImageService = postImageService;
    }

    /**
     * Get posts paged by username
     *
     * @param username the username to get posts from
     * @param pageable pagination parameters
     * @return Page<Post>
     */
    public Page<Post> getPostsByUsername(String username, Pageable pageable) {
        log.debug("Fetching posts from username: {}", username);
        // check if the customer exists by this username
        profileRepository.findByUsernameIgnoreCase(username).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND, username)
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
        log.debug("Customer: {} creating a new post.", currentCustomer.getId());

        // create the post
        Post post = Post.create(currentCustomer)
                        .setImageFilename(request.imageFilename())
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
     * @param postId the id of the post to be deleted.
     * @throws PostNotFoundException  if the post does not exist.
     * @throws PostOwnershipException if the current customer is not the author of the post.
     */
    public void deletePost(Long postId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Customer: {} deleting post:{} ", currentCustomer.getId(), postId);

        // check if the post exists
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POST.NOT_FOUND, postId)
        );

        // check if the current customer is the owner of the post.
        if (!post.isAuthor(currentCustomer)) {
            throw new PostOwnershipException(Exceptions.POST.NOT_AUTHOR, postId, currentCustomer.getId());
        }

        // delete the image from the storage.
        postImageService.deleteImage(postId);

        // delete the post from the database.
        postRepository.deleteById(postId);
    }
}
