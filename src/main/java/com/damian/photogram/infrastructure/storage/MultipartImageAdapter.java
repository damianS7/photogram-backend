package com.damian.photogram.infrastructure.storage;

import com.damian.photogram.core.util.ImageHelper;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class MultipartImageAdapter implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public MultipartImageAdapter(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    public MultipartImageAdapter(MultipartFile multipartFile) {
        this.name = multipartFile.getName();
        this.originalFilename = multipartFile.getOriginalFilename();
        this.contentType = multipartFile.getContentType();
        try {
            this.content = multipartFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultipartImageAdapter(File file) {
        this.name = file.getName();
        this.originalFilename = file.getName();
        this.contentType = ImageHelper.getContentType(file);
        try (FileInputStream fis = new FileInputStream(file)) {
            this.content = fis.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}