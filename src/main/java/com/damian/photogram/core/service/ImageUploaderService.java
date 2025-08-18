package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.customer.exception.ProfileAuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageUploaderService {
    private final String IMAGE_PATH = "uploads/images/";
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public ImageUploaderService(
    ) {
    }

    // validations for file uploaded photos
    public void validateImageOrElseThrow(MultipartFile file) {
        // TODO create exceptions
        if (file.isEmpty()) {
            throw new ProfileAuthorizationException(
                    Exceptions.IMAGE.EMPTY_FILE
            );
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new ProfileAuthorizationException(
                    Exceptions.IMAGE.ONLY_IMAGES_ALLOWED
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ProfileAuthorizationException(
                    Exceptions.IMAGE.FILE_SIZE_LIMIT
            );
        }
    }

    // stores the image
    private void storeFile(MultipartFile file, String folder, String filename) {
        try {
            Path uploadPath = Paths.get(IMAGE_PATH + folder);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ProfileAuthorizationException(
                    Exceptions.IMAGE.UPLOAD_FAILED
            );
        }
    }

    /**
     * Uploads an image to the server
     */
    public String uploadImage(MultipartFile file, String folder, String filename) {
        // run file validations
        this.validateImageOrElseThrow(file);
        // TODO compress or convert to webp image before uploading to server

        final String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        filename += "." + extension;

        // saving file
        this.storeFile(file, folder, filename);

        return filename;
    }

    /**
     * Uploads an image to the server
     */
    public String uploadImage(MultipartFile file, String folder) {
        String filename = UUID.randomUUID().toString();
        return this.uploadImage(file, folder, filename);
    }
}
