package com.damian.photogram.core.image.adapter;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ImageFileAdapter implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public ImageFileAdapter(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    public ImageFileAdapter(MultipartFile multipartFile) throws IOException {
        this.name = multipartFile.getName();
        this.originalFilename = multipartFile.getOriginalFilename();
        this.contentType = multipartFile.getContentType();
        this.content = multipartFile.getBytes();
    }

    public ImageFileAdapter(File file) throws IOException {
        this.name = file.getName();
        this.originalFilename = file.getName();
        this.contentType = "";
        this.content = new FileInputStream(file).readAllBytes();
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