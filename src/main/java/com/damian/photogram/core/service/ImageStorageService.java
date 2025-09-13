package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageFailedUploadException;
import com.damian.photogram.core.exception.ImageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

/**
 * Service class for handling image storage and retrieval.
 */
@Service
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

    /**
     * Creates a Resource from the given path.
     * Path must be a valid path to an existing file.
     *
     * @param path the path of the image
     * @return Resource object representing the image
     */
    public Resource createResource(Path path) {
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }

        return resource;
    }

    // stores the image
    public void storeImage(MultipartFile file, String path, String filename) {
        if (!path.startsWith(ImageUploaderService.ROOT_UPLOAD_FOLDER)) {
            throw new ImageFailedUploadException(Exceptions.IMAGE.UPLOAD_FAILED);
        }

        try {
            Path uploadPath = Paths.get(path);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO chck the exception type
            throw new ImageFailedUploadException(
                    Exceptions.IMAGE.UPLOAD_FAILED
            );
        }
    }

    /**
     * Returns a resource for the given folder and filename.
     *
     * @param folderPath path where image is stored
     * @param filename   name of the image
     * @return Resource object representing the image
     */
    public Resource getImage(String folderPath, String filename) {
        log.info("Fetching image from folder: {}, filename: {}", folderPath, filename);

        if (!folderPath.startsWith(ImageUploaderService.ROOT_UPLOAD_FOLDER)) {
            throw new ImageNotFoundException(Exceptions.IMAGE.INVALID_PATH);
        }

        Path filePath;
        try {
            filePath = Paths.get(folderPath).resolve(filename).normalize();
        } catch (InvalidPathException exception) {
            throw new ImageNotFoundException(Exceptions.IMAGE.INVALID_PATH);
        }

        Resource resource = this.createResource(filePath);

        if (!resource.exists()) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }

        return resource;
    }

    /**
     * Delete an image from server storage
     *
     * @param folder   folder where the image is
     * @param filename name of the image
     */
    public void deleteImage(String folder, String filename) {
        try {
            Path pathToFile = Path.of(folder + "/" + filename);
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }
    }
}
