package com.judgeai.interviewanalyzer.service;

import com.judgeai.interviewanalyzer.dto.InterviewResponse;
import com.judgeai.interviewanalyzer.entity.Interview;
import com.judgeai.interviewanalyzer.exception.InvalidFileException;
import com.judgeai.interviewanalyzer.repository.InterviewRepository;
import com.judgeai.interviewanalyzer.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final StorageService storageService;

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("video/mp4", "video/webm", "video/ogg", "video/quicktime");

    public InterviewResponse uploadInterview(MultipartFile file) {
        log.info("Received upload request for file: {}, size: {}", file.getOriginalFilename(), file.getSize());

        validateFile(file);

        String url = storageService.store(file);

        Interview interview = Interview.builder()
                .filename(file.getOriginalFilename())
                .s3Url(url)
                .status("UPLOADED")
                .build();

        Interview savedInterview = interviewRepository.save(interview);
        log.info("Interview metadata saved with ID: {}", savedInterview.getId());

        return mapToResponse(savedInterview);
    }

    public InterviewResponse getInterview(UUID id) {
        log.debug("Fetching interview with ID: {}", id);
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new InvalidFileException("Interview not found with ID: " + id));
        return mapToResponse(interview);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType)) {
            log.warn("Invalid file type attempted: {}", contentType);
            throw new InvalidFileException("Only video files (MP4, WebM, OGG, MOV) are allowed");
        }
    }

    private InterviewResponse mapToResponse(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .filename(interview.getFilename())
                .url(interview.getS3Url())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .build();
    }
}
