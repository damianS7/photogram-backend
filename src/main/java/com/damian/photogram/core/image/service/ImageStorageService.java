package com.damian.photogram.core.image.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.image.exception.ImageFailedStorageException;
import com.damian.photogram.core.image.exception.ImageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

/**
 * Service class for handling image storage and retrieval.
 */
@Service
public class ImageStorageService {
    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
    private final String ROOT_STORAGE_PATH = "uploads";

    public String getRootStoragePath() {
        return Paths.get(ROOT_STORAGE_PATH).toAbsolutePath().toString();
    }

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

    /**
     * Stores the given image file in the specified path with the provided filename.
     *
     * @param file     the image file to be stored
     * @param path     the directory path where the image will be stored
     * @param filename the name to be assigned to the stored image file
     * @return File object representing the stored image
     */
    public File storeImage(MultipartFile file, String path, String filename) {
        path = getRootStoragePath() + "/" + path;
        log.info("Storing image to path: {}, with filename: {}", path, filename);

        try {
            Path storePath = Paths.get(path);
            Files.createDirectories(storePath);
            Path filePath = storePath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toFile();
        } catch (IOException e) {
            throw new ImageFailedStorageException(Exceptions.IMAGE.STORAGE_FAILED, e);
        }
    }

    /**
     * Returns a resource for the given folder and filename.
     *
     * @param pathToImage path where image is stored
     * @param filename    name of the image
     * @return Resource object representing the image
     */
    public Resource getImage(String pathToImage, String filename) {
        pathToImage = getRootStoragePath() + "/" + pathToImage;
        log.info("Fetching image from folder: {}, filename: {}", pathToImage, filename);

        Path filePath;
        try {
            filePath = Paths.get(pathToImage).resolve(filename).normalize();
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
     * @param pathToImage folder where the image is
     * @param filename    name of the image
     */
    public void deleteImage(String pathToImage, String filename) {
        pathToImage = getRootStoragePath() + "/" + pathToImage;
        log.info("Deleting image from folder: {}, filename: {}", pathToImage, filename);
        try {
            Path pathToFile = Path.of(pathToImage + "/" + filename);
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }
    }
}
