package com.judgeai.interviewanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InterviewResponse {
    private UUID id;
    private String filename;
    private String url;
    private String status;
    private LocalDateTime createdAt;
}
