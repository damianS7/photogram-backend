package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service class for handling image storage and retrieval.
 */
@Service
public class ImageStorageService {
    private final String UPLOAD_IMAGE_PATH = "uploads/images/";

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
     * Returns a resource for the given folder and filename.
     *
     * @param folderPath path where image is stored
     * @param filename   name of the image
     * @return Resource object representing the image
     */
    public Resource getImage(String folderPath, String filename) {
        Path filePath;
        try {
            filePath = Paths.get(UPLOAD_IMAGE_PATH + folderPath).resolve(filename).normalize();
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
            Path pathToFile = Path.of(UPLOAD_IMAGE_PATH + folder + "/" + filename);
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }
    }
}
