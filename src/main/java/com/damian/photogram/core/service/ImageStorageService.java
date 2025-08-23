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
    private final String ROOT_PATH = "uploads/images/";

    // creates a Resource from the given path
    public Resource createResource(Path path) {
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }

        return resource;
    }

    // returns the image as Resource
    public Resource getImage(String folderPath, String filename) {
        Path filePath;
        try {
            filePath = Paths.get(ROOT_PATH + folderPath).resolve(filename).normalize();
        } catch (InvalidPathException exception) {
            throw new ImageNotFoundException(Exceptions.IMAGE.INVALID_PATH);
        }

        Resource resource = this.createResource(filePath);

        if (!resource.exists()) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }

        return resource;
    }

    // delete image from server
    public void deleteImage(String folder, String filename) {
        try {
            Path pathToFile = Path.of(ROOT_PATH + folder + "/" + filename);
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }
    }
}
