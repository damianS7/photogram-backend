package com.damian.photogram.web.user;

import com.damian.photogram.core.util.ImageHelper;
import com.damian.photogram.domain.user.model.Profile;
import com.damian.photogram.service.user.ProfileImageService;
import com.damian.photogram.service.user.ProfileService;
import com.damian.photogram.web.user.dto.mapper.ProfileDtoMapper;
import com.damian.photogram.web.user.dto.request.ProfileUpdateRequest;
import com.damian.photogram.web.user.dto.response.ProfileDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);
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
        log.debug("Received request for getting current customer profile");
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
        log.debug("Received request for checking if username: {} exists", username);
        profileService.userProfileExists(username);

        return ResponseEntity
                .status(HttpStatus.OK).build();
    }

    // endpoint to modify the logged customer profile
    @PatchMapping("/customers/profile")
    public ResponseEntity<?> updateProfile(
            @Validated @RequestBody
            ProfileUpdateRequest request
    ) {
        log.debug("Received request for updating(patch) profile");
        Profile profile = profileService.updateProfile(request);
        ProfileDto profileDTO = ProfileDtoMapper.toProfileDto(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to get the current customer profile image
    @GetMapping("/customers/{customerId}/profile/image")
    public ResponseEntity<?> getProfileImage(
            @PathVariable @NotNull @Positive
            Long customerId
    ) {
        log.debug("Received request getting customer: {} profile image", customerId);
        Resource resource = profileImageService.getProfileImage(customerId);
        String contentType = ImageHelper.getContentType(resource);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    // endpoint for the current customer to upload his profile photo
    @PostMapping("/customers/profile/image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("currentPassword") @NotBlank
            String currentPassword,
            @RequestParam("file") MultipartFile file
    ) {
        log.debug("Received request for updating image profile");
        profileImageService.uploadProfileImage(currentPassword, file);
        Resource resource = profileImageService.getProfileImage();
        String contentType = ImageHelper.getContentType(resource);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}

