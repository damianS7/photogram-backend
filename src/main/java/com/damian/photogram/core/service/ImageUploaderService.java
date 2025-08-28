package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

/**
 * Service class for handling image uploads to the server.
 */
@Service
public class ImageUploaderService {
    private final String IMAGE_PATH = "uploads/images/";
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final String[] ALLOWED_IMAGE_TYPE = {"image/jpg", "image/jpeg", "image/webp"};

    public ImageUploaderService(
    ) {
    }

    // validations for file uploaded photos
    public void validateImageOrElseThrow(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ImageEmptyFileException(Exceptions.IMAGE.EMPTY_FILE);
        }

        String contentType = file.getContentType();
        boolean imageTypeAllowed = Arrays
                .stream(ALLOWED_IMAGE_TYPE)
                .anyMatch(ct -> ct.equalsIgnoreCase(contentType));

        if (!imageTypeAllowed) {
            throw new ImageTypeNotAllowedException(Exceptions.IMAGE.TYPE_NOT_SUPPORTED);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageFileSizeExceededException(Exceptions.IMAGE.TOO_LARGE);
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
            throw new ImageFailedUploadException(
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
        if (!filename.endsWith(extension)) {
            filename += "." + extension;
        }


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
