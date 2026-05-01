package com.judgeai.interviewanalyzer.service.storage;

import com.judgeai.interviewanalyzer.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket-name}")
    private String bucketName;

    @Override
    public String store(MultipartFile file) {
        String filename = generateUniqueFileName(file.getOriginalFilename());
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String s3Url = String.format("https://%s.s3.amazonaws.com/%s", bucketName, filename);
            log.info("File uploaded to S3: {}", s3Url);
            return s3Url;
        } catch (IOException e) {
            throw new StorageException("Failed to upload file to S3", e);
        }
    }
}
