package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.infrastructure.storage.exception.ImageNotFoundException;
import com.damian.photogram.infrastructure.storage.exception.ImageStorageFailedException;
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
public class LocalStorageService {
    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
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

        if (!resource.exists()) {
            throw new ImageNotFoundException(
                    Exceptions.IMAGE.NOT_FOUND,
                    path.toString(),
                    path.getFileName().toString()
            );
        }

        return resource;
    }

    public Resource createResource(File file) {
        return this.createResource(file.toPath());
    }

    /**
     * Stores the given image file in the specified path with the provided filename.
     *
     * @param file     the image file to be stored
     * @param path     the directory path where the image will be stored
     * @param filename the name to be assigned to the stored image file
     * @return File object representing the stored image
     */
    public File storeFile(MultipartFile file, String path, String filename) {
        path = getRootStoragePath() + "/" + path;
        log.info("Storing image: {} within path: {}", filename, path);

        try {
            Path storePath = Paths.get(path);
            Files.createDirectories(storePath);
            Path filePath = storePath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toFile();
        } catch (IOException e) {
            throw new ImageStorageFailedException(Exceptions.IMAGE.STORAGE_FAILED, path);
        }
    }

    /**
     * Returns a resource for the given folder and filename.
     *
     * @param path     path where file is stored
     * @param filename name of the file
     * @return Resource object representing the file
     */
    public File getFile(String path, String filename) {
        path = getRootStoragePath() + "/" + path;
        log.debug("Retrieving file from path: {}, with filename: {}", path, filename);

        Path filePath;
        try {
            filePath = Paths.get(path).resolve(filename).normalize();
            return filePath.toFile();
        } catch (InvalidPathException exception) {
            throw new ImageNotFoundException(Exceptions.IMAGE.INVALID_PATH, path, filename);
        }
    }

    /**
     * Delete a file from server storage
     *
     * @param path     path where the image is
     * @param filename name of the image
     */
    public void deleteFile(String path, String filename) {
        path = getRootStoragePath() + "/" + path;
        try {
            log.debug("Deleting file: {} within: {}", filename, path);
            Path pathToFile = Path.of(path + "/" + filename);
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND, path, filename);
        }
    }
}
