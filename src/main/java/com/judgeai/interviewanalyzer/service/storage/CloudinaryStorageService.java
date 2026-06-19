package com.judgeai.interviewanalyzer.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.judgeai.interviewanalyzer.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Slf4j
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            
            // Create a temporary file to upload
            File tempFile = Files.createTempFile("temp", file.getOriginalFilename()).toFile();
            file.transferTo(tempFile);
            
            log.info("Uploading file to Cloudinary: {}", file.getOriginalFilename());
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap("resource_type", "video"));
            
            // Delete temp file after upload
            tempFile.delete();
            
            String url = uploadResult.get("secure_url").toString();
            log.info("File successfully uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            throw new StorageException("Failed to upload file to Cloudinary", e);
        }
    }
}
