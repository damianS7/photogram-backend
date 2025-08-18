package com.damian.photogram.core.service;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

public class ImageHelper {

    public static String getContentType(Resource resource) {

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
}
