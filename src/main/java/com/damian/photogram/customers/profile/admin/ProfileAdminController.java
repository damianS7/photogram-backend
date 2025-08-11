package com.damian.photogram.customers.profile.admin;

import com.damian.photogram.customers.profile.Profile;
import com.damian.photogram.customers.profile.ProfileDTO;
import com.damian.photogram.customers.profile.ProfileDTOMapper;
import com.damian.photogram.customers.profile.ProfileService;
import com.damian.photogram.customers.profile.http.request.ProfileUpdateRequest;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class ProfileAdminController {
    private final ProfileService profileService;

    @Autowired
    public ProfileAdminController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // endpoint to get the profile of a customers
    @GetMapping("/admin/profiles/{id}")
    public ResponseEntity<?> getCustomerProfile(
            @PathVariable @Positive
            Long id
    ) {
        Profile profile = profileService.getProfile(id);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to modify the fields profile
    @PatchMapping("/admin/profiles/{id}")
    public ResponseEntity<?> updateCustomerProfile(
            @PathVariable @Positive
            Long id,
            @Validated @RequestBody
            ProfileUpdateRequest request
    ) {
        Profile profile = profileService.updateProfile(id, request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }
}

