package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.user.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service class for handling image uploads to the server.
 */
@Service
public class ImageUploaderService {
    public static final String ROOT_UPLOAD_FOLDER = "uploads/images/customers/";
    private static final Logger log = LoggerFactory.getLogger(ImageUploaderService.class);
    private final ImageStorageService imageStorageService;

    public ImageUploaderService(
            ImageStorageService imageStorageService
    ) {
        this.imageStorageService = imageStorageService;
    }

    public static String getCustomerUploadFolder(Long customerId) {
        return ROOT_UPLOAD_FOLDER + customerId + "/";
    }

    /**
     * Uploads an image to the server
     */
    public String uploadImage(MultipartFile file, String folder, String filename) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();
        String path = getCustomerUploadFolder(currentCustomer.getId()) + folder;
        log.debug("Uploading file: {} to: {}", filename, path);

        final String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension != null && !filename.endsWith(extension)) {
            filename += "." + extension;
        }

        // saving file
        imageStorageService.storeImage(file, path, filename);
        return filename;
    }

    /**
     * Uploads an image to the server
     */
    public String uploadImage(MultipartFile file, String folder) {
        String filename = UUID.randomUUID().toString();
        return this.uploadImage(file, folder, filename);
    }
}
