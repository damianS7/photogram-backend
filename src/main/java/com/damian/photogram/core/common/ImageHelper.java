package com.damian.photogram.core.common;

import com.damian.photogram.core.exception.Exceptions;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

public class ImageHelper {

    // TODO catch exception in handler
    public static String getContentType(Resource resource) {

        String contentType;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(Exceptions.OTHER.IMAGE_TYPE_NOT_DETECTED);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return contentType;
    }
}
