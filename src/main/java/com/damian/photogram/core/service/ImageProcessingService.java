package com.damian.photogram.core.service;

import com.damian.photogram.core.exception.ImageCompressionFailedException;
import com.damian.photogram.core.exception.ImageResizeFailedException;
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
 * Service class for compressing images.
 */
@Service
public class ImageProcessingService {
    private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);
    private final float IMAGE_QUALITY = 0.7f; // compression quality (0.0f - 1.0f)

    public ImageProcessingService(
    ) {
    }

    public byte[] compressImage(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream output = ImageIO.createImageOutputStream(baos)) {

            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            jpgWriter.setOutput(output);

            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(IMAGE_QUALITY); // entre 0 y 1

            jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
            jpgWriter.dispose();

            log.info("Image compressed successfully");
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

    public BufferedImage resizeBufferedImage(BufferedImage image, int maxWidth, int maxHeight) {
        final int currentWidth = image.getWidth();
        final int currentHeight = image.getHeight();
        log.debug("Resizing image with original dimensions {}x{}", currentWidth, currentHeight);

        // Resize only if the image exceeds the maximum dimensions
        if (currentWidth < maxWidth && currentHeight < maxHeight) {
            log.debug("Image is within limits ({}x{}), no resize needed", currentWidth, currentHeight);
            return image;
        }


        double widthRatio = (double) maxWidth / currentWidth;
        double heightRatio = (double) maxHeight / currentHeight;
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

    public MultipartFile resizeImageMultipart(MultipartFile file, int maxWidth, int maxHeight) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage resizedImage = resizeBufferedImage(
                    ImageIO.read(file.getInputStream()), maxWidth, maxHeight
            );
            ImageIO.write(resizedImage, "jpg", baos);
        } catch (IOException e) {
            throw new ImageResizeFailedException(e.getMessage());
        }

        return new ImageMultipartAdapter(
                file.getName(),
                file.getOriginalFilename(),
                "image/jpeg",
                baos.toByteArray()
        );
    }

    public MultipartFile resizeImageFile(File file, int maxWidth, int maxHeight) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage resizedImage = resizeBufferedImage(
                    ImageIO.read(file), maxWidth, maxHeight
            );

            ImageIO.write(resizedImage, "jpg", baos);

        } catch (IOException e) {
            throw new ImageResizeFailedException(e.getMessage());
        }
        return new ImageMultipartAdapter(
                file.getName(),
                file.getName(),
                "image/jpeg",
                baos.toByteArray()
        );
    }
}
