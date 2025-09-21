package com.damian.photogram.service.post.post;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.util.ImageTestHelper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.infrastructure.storage.FileStorageService;
import com.damian.photogram.infrastructure.storage.ImageProcessingService;
import com.damian.photogram.infrastructure.storage.ImageUploaderService;
import com.damian.photogram.infrastructure.storage.ImageValidationService;
import com.damian.photogram.service.post.PostImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PostImageServiceTest extends AbstractServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageProcessingService imageProcessingService;

    @Mock
    private ImageValidationService imageValidationService;

    @InjectMocks
    private PostImageService postImageService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.create()
                           .setId(2L)
                           .setEmail("customer@test.com")
                           .setPassword(passwordEncoder.encode(RAW_PASSWORD))
                           .setRole(UserRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setId(5L)
                                   .setUsername("John")
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
    }

    @Test
    @DisplayName("Should upload post image")
    void shouldUploadPostImage() throws IOException {
        // given
        setUpContext(customer);
        
        MockMultipartFile multipartFile = ImageTestHelper.createDefaultJpg();
        File givenFile = ImageTestHelper.multipartToFile(
                multipartFile
        );

        // when
        doNothing().when(imageValidationService).validateImage(any(), any(Long.class), any(String[].class));
        when(imageProcessingService.optimizeImage(any(), any(Integer.class), any(Integer.class))).thenReturn(
                multipartFile);
        when(imageUploaderService.uploadImage(
                any(MultipartFile.class),
                anyString()
        )).thenReturn(givenFile);

        File uploadedImage = postImageService.uploadImage(
                multipartFile
        );

        // then
        assertNotNull(uploadedImage);
        assertEquals(givenFile.length(), uploadedImage.length());
    }

    @Test
    @DisplayName("Should get post image")
    void shouldGetPostImage() throws IOException {
        // given
        setUpContext(customer);
        MockMultipartFile givenMultipartFile = ImageTestHelper.createDefaultJpg();
        File givenFile = ImageTestHelper.multipartToFile(givenMultipartFile);

        Post givenPost = Post.create(customer)
                             .setId(1L)
                             .setImageFilename(givenMultipartFile.getOriginalFilename())
                             .setDescription("qsdfsdf");

        Resource givenResource = new ByteArrayResource(givenMultipartFile.getBytes());

        // when
        when(postRepository.findById(givenPost.getId())).thenReturn(Optional.of(givenPost));
        when(fileStorageService.getFile(anyString(), anyString())).thenReturn(givenFile);
        when(fileStorageService.createResource(givenFile)).thenReturn(givenResource);
        Resource resource = postImageService.getImage(
                givenPost.getId()
        );

        // then
        assertNotNull(resource);
        assertArrayEquals(givenMultipartFile.getBytes(), resource.getContentAsByteArray());
        verify(postRepository, times(1)).findById(givenPost.getId());
        verify(fileStorageService, times(1)).getFile(anyString(), anyString());
    }

    @Test
    @DisplayName("Should delete post image")
    void shouldDeletePostImage() throws IOException {
        // given
        setUpContext(customer);
        MockMultipartFile givenFile = ImageTestHelper.createDefaultJpg();

        Post givenPost = Post.create(customer)
                             .setId(1L)
                             .setImageFilename(givenFile.getOriginalFilename())
                             .setDescription("qsdfsdf");

        // when
        when(postRepository.findById(givenPost.getId())).thenReturn(Optional.of(givenPost));
        doNothing().when(fileStorageService).deleteFile(anyString(), anyString());
        postImageService.deleteImage(
                givenPost.getId()
        );

        // then
        verify(postRepository, times(1)).findById(givenPost.getId());
        verify(fileStorageService, times(1)).deleteFile(anyString(), anyString());
    }
}
