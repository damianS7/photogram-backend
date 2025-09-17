package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.user.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service class for handling image uploads to the server.
 */
@Service
public class ImageUploaderService {
    public static final String UPLOAD_PATH = "uploads/images/customers/{customerId}";
    private static final Logger log = LoggerFactory.getLogger(ImageUploaderService.class);
    private final FileStorageService fileStorageService;

    public ImageUploaderService(
            FileStorageService fileStorageService
    ) {
        this.fileStorageService = fileStorageService;
    }

    public static String getCustomerUploadFolder(Long customerId) {
        return UPLOAD_PATH.replace("{customerId}", customerId.toString());
    }

    /**
     * Uploads an image to the server
     */
    public File uploadImage(MultipartFile file, String folder, String filename) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();
        Path path = Paths.get(
                getCustomerUploadFolder(currentCustomer.getId()),
                folder
        );
        log.debug("Uploading file: {} to: {}", filename, path);

        final String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension != null && !filename.endsWith(extension)) {
            filename += "." + extension;
        }

        // saving file
        return fileStorageService.storeFile(file, String.valueOf(path), filename);
    }

    /**
     * Uploads an image to the server
     */
    public File uploadImage(MultipartFile file, String folder) {
        String filename = UUID.randomUUID().toString();
        return this.uploadImage(file, folder, filename);
    }
}
