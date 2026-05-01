package com.judgeai.interviewanalyzer.controller;

import com.judgeai.interviewanalyzer.dto.InterviewResponse;
import com.judgeai.interviewanalyzer.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/upload")
    public ResponseEntity<InterviewResponse> uploadVideo(@RequestParam("video") MultipartFile file) {
        log.info("REST request to upload video: {}", file.getOriginalFilename());
        InterviewResponse response = interviewService.uploadInterview(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interview/{id}")
    public ResponseEntity<InterviewResponse> getInterview(@PathVariable UUID id) {
        log.info("REST request to get interview: {}", id);
        InterviewResponse response = interviewService.getInterview(id);
        return ResponseEntity.ok(response);
    }
}
