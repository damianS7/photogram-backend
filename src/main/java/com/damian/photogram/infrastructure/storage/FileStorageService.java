package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.infrastructure.storage.exception.FileStorageException;
import com.damian.photogram.infrastructure.storage.exception.FileStorageFailedException;
import com.damian.photogram.infrastructure.storage.exception.FileStorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service class for handling file storage and retrieval.
 */
@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final String STORAGE_FOLDER = "storage";

    public Path getStoragePath(String path) {
        return Paths.get(System.getProperty("user.dir"), STORAGE_FOLDER, path)
                    .toAbsolutePath()
                    .normalize();
    }

    /**
     * Creates a Resource from the given path.
     * Path must be a valid path to an existing file.
     *
     * @param path the path of the file
     * @return Resource object representing the file
     */
    public Resource createResource(Path path) {
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new FileStorageNotFoundException(
                    Exceptions.STORAGE.NOT_FOUND,
                    path.toAbsolutePath().toString(),
                    path.getFileName().toString()
            );
        }

        if (!resource.exists()) {
            throw new FileStorageNotFoundException(
                    Exceptions.STORAGE.NOT_FOUND,
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
     * Stores the given file at the specified path with the provided filename.
     *
     * @param file     the file to be stored
     * @param path     the directory path where the file will be stored
     * @param filename the name to be assigned to the stored file
     * @return File the stored file.
     */
    public File storeFile(MultipartFile file, String path, String filename) {
        Path filePath = getStoragePath(path).resolve(filename).normalize();
        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageFailedException(Exceptions.STORAGE.FAILED, path, filename);
        }

        log.debug("Stored file: {} at: {}", filename, filePath.getParent());
        return filePath.toFile();
    }

    /**
     * Returns a resource for the given folder and filename.
     *
     * @param path     path where file is stored
     * @param filename name of the file
     * @return Resource object representing the file
     */
    public File getFile(String path, String filename) {
        Path filePath = getStoragePath(path).resolve(filename).normalize();

        // check if file actually exists
        if (!filePath.toFile().exists()) {
            throw new FileStorageNotFoundException(Exceptions.STORAGE.NOT_FOUND, filePath.toString(), filename);
        }

        log.debug("Retrieving file: {} at: {}", filename, filePath.getParent());
        return filePath.toFile();
    }

    /**
     * Delete a file from server storage
     *
     * @param path     path where the file is
     * @param filename name of the file to delete
     */
    public void deleteFile(String path, String filename) {
        Path filePath = getStoragePath(path).resolve(filename).normalize();

        try {
            boolean fileDeleted = Files.deleteIfExists(filePath);
            if (fileDeleted) {
                log.debug("Deleted file: {} at: {}", filename, filePath.getParent());
            }
        } catch (IOException e) {
            throw new FileStorageException(
                    Exceptions.STORAGE.INVALID_PATH,
                    filePath.getParent().toString(),
                    filename
            );
        }

    }
}
