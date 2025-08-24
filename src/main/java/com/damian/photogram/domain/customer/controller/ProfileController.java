package com.damian.photogram.domain.customer.controller;

import com.damian.photogram.core.service.ImageHelper;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.dto.response.ProfileDto;
import com.damian.photogram.domain.customer.mapper.ProfileDtoMapper;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.service.ProfileImageService;
import com.damian.photogram.domain.customer.service.ProfileService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RequestMapping("/api/v1")
@RestController
public class ProfileController {
    private final ProfileService profileService;
    private final ProfileImageService profileImageService;

    @Autowired
    public ProfileController(
            ProfileService profileService,
            ProfileImageService profileImageService
    ) {
        this.profileService = profileService;
        this.profileImageService = profileImageService;
    }

    // endpoint to get the current customer's profile
    @GetMapping("/customers/profile")
    public ResponseEntity<?> getCustomerProfile() {
        Profile profile = profileService.getProfile();
        ProfileDto profileDTO = ProfileDtoMapper.toProfileDto(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to check if a username exists
    @GetMapping("/customers/profile/username/{username}/exists")
    public ResponseEntity<?> usernameExists(
            @PathVariable @NotBlank
            String username
    ) {
        profileService.usernameExists(username);

        return ResponseEntity
                .status(HttpStatus.OK).build();
    }

    // endpoint to modify the logged customer profile
    @PatchMapping("/customers/profile")
    public ResponseEntity<?> updateProfile(
            @Validated @RequestBody
            ProfileUpdateRequest request
    ) {
        Profile profile = profileService.updateProfile(request);
        ProfileDto profileDTO = ProfileDtoMapper.toProfileDto(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to get the logged customer profile photo
    @GetMapping("/customers/{customerId}/profile/photo")
    public ResponseEntity<?> getProfilePhoto(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        Resource resource = profileImageService.getProfileImage(customerId);
        String contentType = ImageHelper.getContentType(resource);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    // endpoint for the current customer to upload his profile photo
    @PostMapping("/customers/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("currentPassword") @NotBlank
            String currentPassword,
            @RequestParam("file") MultipartFile file
    ) {
        profileImageService.uploadImage(currentPassword, file);
        Resource resource = profileImageService.getProfileImage();
        String contentType = ImageHelper.getContentType(resource);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}

