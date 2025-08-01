package com.damian.photogram.customer.profile;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.profile.exception.ProfileAuthorizationException;
import com.damian.photogram.customer.profile.exception.ProfileException;
import com.damian.photogram.customer.profile.http.request.ProfileUpdateRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileImageUploaderService {
    private final String PROFILE_IMAGE_PATH = "uploads/profile/images";
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    public ProfileImageUploaderService(
            ProfileRepository profileRepository,
            ProfileService profileService
    ) {
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }

    public String getContentType(Resource resource) {

        String contentType = null;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return contentType;
    }

    // validations for file uploaded photos
    private void validatePhotoOrElseThrow(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.EMPTY_FILE
            );
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.ONLY_IMAGES_ALLOWED
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) { // 5 MB
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.FILE_SIZE_LIMIT
            );
        }
    }

    // stores the image
    private void storeFile(MultipartFile file, String filename) {
        try {
            Path uploadPath = Paths.get(PROFILE_IMAGE_PATH);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.UPLOAD_FAILED
            );
        }
    }

    public Resource createResource(Path path) {
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ProfileException(Exceptions.PROFILE.IMAGE.NOT_FOUND);
        }

        return resource;
    }

    // returns the profile photo as Resource
    public Resource getImage(String filename) {
        Path filePath = Paths.get(PROFILE_IMAGE_PATH).resolve(filename).normalize();
        Resource resource = this.createResource(filePath);

        if (!resource.exists()) {
            throw new ProfileException(
                    Exceptions.PROFILE.IMAGE.NOT_FOUND
            );
        }
        return resource;
    }

    /**
     * It sets the customer profile photo
     */
    public Resource uploadImage(String currentPassword, MultipartFile file) {
        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        // validate password
        AuthHelper.validatePassword(customerLogged, currentPassword);

        // run file validations
        this.validatePhotoOrElseThrow(file);

        final String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        final String filename = UUID.randomUUID() + "." + extension;

        // saving file
        this.storeFile(file, filename);

        Map<String, Object> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("avatarFilename", filename);
        ProfileUpdateRequest patchRequest = new ProfileUpdateRequest(
                currentPassword,
                fieldsToUpdate
        );

        final Long profileId = customerLogged.getProfile().getId();
        profileService.updateProfile(profileId, patchRequest);
        return this.getImage(filename);
    }
}
