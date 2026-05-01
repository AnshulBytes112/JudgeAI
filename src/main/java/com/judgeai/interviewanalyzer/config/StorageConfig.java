package com.judgeai.interviewanalyzer.config;

import com.judgeai.interviewanalyzer.service.storage.LocalStorageService;
import com.judgeai.interviewanalyzer.service.storage.S3StorageService;
import com.judgeai.interviewanalyzer.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "local")
    public StorageService localStorageService(@Value("${storage.local.path}") String path) {
        return new LocalStorageService(path);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public StorageService s3StorageService(S3Client s3Client) {
        return new S3StorageService(s3Client);
    }
}
