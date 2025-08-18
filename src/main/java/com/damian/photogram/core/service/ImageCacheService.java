package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageCacheService {
    private final String ROOT_PATH = "uploads/images/";

    public Resource createResource(Path path) {
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new ImageNotFoundException(Exceptions.IMAGE.NOT_FOUND);
        }

        return resource;
    }

    //     returns the profile photo as Resource
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

    // endpoint to get the logged customer profile photo
    //    @GetMapping("/customer/profile/photo1111/{filename:.+}")
    //    public ResponseEntity<?> getCustomerProfilePhoto(
    //            @PathVariable @NotBlank
    //            String filename,
    //            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    //    ) throws IOException {
    //        Resource resource = profileImageUploaderService.getImage(filename);
    //        final byte[] content = resource.getInputStream().readAllBytes();
    //        String eTag = "\"" + DigestUtils.md5DigestAsHex(content) + "\"";
    //
    //        if (eTag.equals(ifNoneMatch)) {
    //            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(eTag).build();
    //        }
    //
    //        String contentType = profileImageUploaderService.getContentType(resource);
    //
    //        return ResponseEntity
    //                .status(HttpStatus.OK)
    //                .eTag(eTag)
    //                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
    //                .contentType(MediaType.parseMediaType(contentType))
    //                .body(resource);
    //    }


}
