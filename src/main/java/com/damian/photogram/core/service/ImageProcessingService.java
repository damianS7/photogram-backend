package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.ImageCompressionFailedException;
import com.damian.photogram.core.image.adapter.ImageMultipartAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Service class for processing images. Mostly compressing and resizing.
 */
@Service
public class ImageProcessingService {
    private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);
    private final int COMPRESSION_TRIGGER = 150 * 1024; // 500 kb
    private final float IMAGE_QUALITY = 0.7f; // compression quality (0.0f - 1.0f)

    public BufferedImage multipartToBufferedImage(MultipartFile file) {
        try {
            return ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage fileToBufferedImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File bufferedImageToFile(File file, BufferedImage image) {
        try {
            ImageIO.write(image, "jpg", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public MultipartFile bufferedImageToMultipart(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);
            return new ImageMultipartAdapter(
                    "image",
                    "image.jpg",
                    "image/jpeg",
                    baos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Optimize an image by resizing and compressing it if necessary.
     *
     * @param file
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public MultipartFile optimizeImage(MultipartFile file, int maxWidth, int maxHeight) {
        log.info("Optimizing image with size: {} bytes", file.getSize());
        BufferedImage image = multipartToBufferedImage(file);

        // Resize if the image exceeds the maximum dimensions
        if (isImageResolutionExceeded(image, maxWidth, maxHeight)) {
            image = resizeBufferedImage(image, maxWidth, maxHeight);
            file = bufferedImageToMultipart(image);
        }

        // Compress if the image size exceeds the trigger
        if (file.getSize() >= COMPRESSION_TRIGGER) {
            file = compressImage(file);
        }

        return file;
    }

    // COMPRESS METHODS

    public byte[] compressImage(BufferedImage image) {
        log.info("Compressing image with size: {} bytes", image.getData().getDataBuffer().getSize());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream output = ImageIO.createImageOutputStream(baos)) {

            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            jpgWriter.setOutput(output);

            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(IMAGE_QUALITY); // entre 0 y 1

            jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
            jpgWriter.dispose();

            log.info("Image compressed successfully into {} bytes", baos.size());
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ImageCompressionFailedException(e.getMessage());
        }
    }

    public MultipartFile compressImage(MultipartFile multipartFile) {
        try {
            final byte[] compressedImage = compressImage(
                    ImageIO.read(multipartFile.getInputStream())
            );

            return new ImageMultipartAdapter(
                    multipartFile.getName(),
                    multipartFile.getOriginalFilename(),
                    "image/jpeg",
                    compressedImage
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultipartFile compressImage(File file) {
        return compressImage(new ImageMultipartAdapter(file));
    }

    // RESIZE METHODS

    public BufferedImage resizeBufferedImage(BufferedImage image, int targetWidth, int targetHeight) {
        final int currentWidth = image.getWidth();
        final int currentHeight = image.getHeight();

        log.info(
                "Resizing image with original dimensions {}x{} to {}x{}",
                currentWidth,
                currentHeight,
                targetWidth,
                targetHeight
        );

        double widthRatio = (double) targetWidth / currentWidth;
        double heightRatio = (double) targetHeight / currentHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (currentWidth * scale);
        int newHeight = (int) (currentHeight * scale);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();

        log.info("Image resized successfully");
        return resizedImage;
    }

    public MultipartFile resizeImageMultipart(MultipartFile file, int targetWidth, int targetHeight) {
        BufferedImage image = multipartToBufferedImage(file);
        BufferedImage resizedImage = resizeBufferedImage(image, targetWidth, targetHeight);

        // If no resizing was needed, return the original file
        if (resizedImage == image) {
            return file;
        }

        return bufferedImageToMultipart(resizedImage);
    }

    public File resizeImageFile(File file, int targetWidth, int targetHeight) {
        BufferedImage image = fileToBufferedImage(file);
        BufferedImage resizedImage = resizeBufferedImage(image, targetWidth, targetHeight);

        // If no resizing was needed, return the original file
        if (resizedImage == image) {
            return file;
        }

        return bufferedImageToFile(file, resizedImage);
    }

    public boolean isImageResolutionExceeded(BufferedImage image, int maxWidth, int maxHeight) {
        final int currentWidth = image.getWidth();
        final int currentHeight = image.getHeight();
        log.info(
                "Checking image dimensions ({}x{}). Limits ({}x{})",
                currentWidth,
                currentHeight,
                maxWidth,
                maxHeight
        );

        // Resize only if the image exceeds the maximum dimensions
        if (currentWidth <= maxWidth && currentHeight <= maxHeight) {
            log.info("Image is within limits ({}x{}), no resize needed", currentWidth, currentHeight);
            return false;
        }
        return true;
    }
}
