package com.damian.photogram.customer.profile;

import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customer.profile.http.request.ProfileUpdateRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/v1")
@RestController
public class ProfileController {
    private final ProfileService profileService;
    private final ProfileImageUploaderService profileImageUploaderService;

    @Autowired
    public ProfileController(
            ProfileService profileService,
            ProfileImageUploaderService profileImageUploaderService
    ) {
        this.profileService = profileService;
        this.profileImageUploaderService = profileImageUploaderService;
    }

    @GetMapping("/customers/me/profile")
    public ResponseEntity<?> getLoggedCustomerProfile() {
        long profileId = AuthHelper.getLoggedCustomer().getProfile().getId();

        Profile profile = profileService.getProfile(profileId);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to modify the logged customer profile
    @PatchMapping("/customers/me/profile")
    public ResponseEntity<?> updateLoggedCustomerProfile(
            @Validated @RequestBody
            ProfileUpdateRequest request
    ) {
        Profile profile = profileService.updateProfile(request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to get the logged customer profile photo
    @GetMapping("/customers/me/profile/photo/{filename:.+}")
    public ResponseEntity<?> getLoggedCustomerPhoto(
            @PathVariable @NotBlank
            String filename
    ) {
        Resource resource = profileImageUploaderService.getImage(filename);
        String contentType = profileImageUploaderService.getContentType(resource);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // endpoint to upload profile photo
    @PostMapping("/customers/me/profile/photo")
    public ResponseEntity<?> uploadLoggedCustomerPhoto(
            @RequestParam("currentPassword") @NotBlank
            String currentPassword,
            @RequestParam("file") MultipartFile file
    ) {
        Resource resource = profileImageUploaderService.uploadImage(currentPassword, file);
        String contentType = profileImageUploaderService.getContentType(resource);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}

