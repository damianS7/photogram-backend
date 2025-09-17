package com.damian.photogram.infrastructure.storage.exception;

public class FileStorageNotFoundException extends FileStorageException {

    public FileStorageNotFoundException(String message, String path, String fileName) {
        super(message, path, fileName);
    }

    public FileStorageNotFoundException(String message) {
        this(message, null, null);
    }

}
