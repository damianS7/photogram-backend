package com.damian.photogram;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageTestHelper {

    public static MockMultipartFile createMockImage(
            String name,
            String filename,
            String format,
            int width,
            int height,
            Color fillColor
    ) {
        try {
            // Crear la imagen en memoria
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(fillColor);
            g.fillRect(0, 0, width, height);
            g.dispose();

            // Convertir a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            byte[] bytes = baos.toByteArray();

            return new MockMultipartFile(name, filename, "image/" + format, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Error creando imagen mock", e);
        }
    }

    public static File multipartToFile(MultipartFile image) {
        try {
            Path tempFile = Files.createTempFile("-", image.getOriginalFilename());
            Files.write(tempFile, image.getBytes());
            return tempFile.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MockMultipartFile createDefaultJpg() {
        return createMockImage("file", "mock-image.jpg", "jpeg", 200, 200, Color.BLUE);
    }

    public static MockMultipartFile createDefaultPng() {
        return createMockImage("file", "mock-image.png", "png", 200, 200, Color.BLUE);
    }

    public static MockMultipartFile createDefaultBmp() {
        return createMockImage("file", "mock-image.webp", "bmp", 200, 200, Color.BLUE);
    }
}