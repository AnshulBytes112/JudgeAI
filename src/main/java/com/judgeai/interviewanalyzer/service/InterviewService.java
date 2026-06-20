package com.judgeai.interviewanalyzer.service;

import com.judgeai.interviewanalyzer.dto.AnalysisResultDto;
import com.judgeai.interviewanalyzer.dto.InterviewResponse;
import com.judgeai.interviewanalyzer.dto.NlpScoreRequestDto;
import com.judgeai.interviewanalyzer.dto.NlpScoreResponseDto;
import com.judgeai.interviewanalyzer.entity.Interview;
import com.judgeai.interviewanalyzer.exception.InvalidFileException;
import com.judgeai.interviewanalyzer.repository.InterviewRepository;
import com.judgeai.interviewanalyzer.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final StorageService storageService;
    private final InterviewRepository interviewRepository;
    private final SpeechAnalyzerClient speechAnalyzerClient;
    private final NlpScorerClient nlpScorerClient;

    private static final List<String> ALLOWED_VIDEO_TYPES = List.of(
            "video/mp4", "video/webm", "video/ogg", "video/quicktime"
    );

    public InterviewResponse uploadInterview(MultipartFile file, String question, String idealAnswer, List<String> keywords) {
        log.info("Received upload request for file: {}, size: {}", file.getOriginalFilename(), file.getSize());

        validateFile(file);

        String url = storageService.store(file);

        Interview interview = Interview.builder()
                .filename(file.getOriginalFilename())
                .videoUrl(url)
                .status("ANALYZING")
                .build();

        Interview savedInterview = interviewRepository.save(interview);
        log.info("Interview metadata saved with ID: {}", savedInterview.getId());

        // Trigger async analysis
        processVideoAsync(savedInterview.getId(), url, question, idealAnswer, keywords);

        return mapToResponse(savedInterview);
    }

    @Async
    public void processVideoAsync(UUID interviewId, String videoUrl, String question, String idealAnswer, List<String> keywords) {
        log.info("Starting async video analysis for interview: {}", interviewId);
        try {
            AnalysisResultDto result = speechAnalyzerClient.analyze(videoUrl);
            
            Interview interview = interviewRepository.findById(interviewId)
                    .orElseThrow(() -> new RuntimeException("Interview not found: " + interviewId));
                    
            interview.setTranscript(result.getTranscript());
            interview.setWpm(result.getWpm());
            if (result.getFillerWords() != null) {
                interview.setFillerWordsCount(result.getFillerWords().getTotal());
            }
            if (result.getPauses() != null) {
                interview.setPausesCount(result.getPauses().getCount());
                interview.setAvgPauseDuration(result.getPauses().getAvgDuration());
            }
            interview.setDurationSeconds(result.getDurationSeconds());

            // Call NLP Scorer
            try {
                NlpScoreRequestDto nlpRequest = NlpScoreRequestDto.builder()
                        .transcript(result.getTranscript())
                        .question(question)
                        .idealAnswer(idealAnswer)
                        .keywords(keywords)
                        .build();

                NlpScoreResponseDto nlpResponse = nlpScorerClient.scoreContent(nlpRequest);
                
                interview.setOverallScore(nlpResponse.getRelevanceScore());
                interview.setSemanticSimilarity(nlpResponse.getCosineSimilarity());
                interview.setKeywordCoverage(nlpResponse.getKeywordCoverage());
                
            } catch (Exception nlpException) {
                log.error("Failed to get NLP scores for interview: {}", interviewId, nlpException);
                // We don't fail the whole process if just NLP fails, but maybe set status to PARTIAL
            }

            interview.setStatus("COMPLETED");
            
            interviewRepository.save(interview);
            log.info("Successfully analyzed and updated interview: {}", interviewId);
        } catch (Exception e) {
            log.error("Failed to analyze video for interview: {}", interviewId, e);
            interviewRepository.findById(interviewId).ifPresent(interview -> {
                interview.setStatus("FAILED");
                interviewRepository.save(interview);
            });
        }
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
                .url(interview.getVideoUrl())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .transcript(interview.getTranscript())
                .wpm(interview.getWpm())
                .fillerWordsCount(interview.getFillerWordsCount())
                .pausesCount(interview.getPausesCount())
                .avgPauseDuration(interview.getAvgPauseDuration())
                .durationSeconds(interview.getDurationSeconds())
                .overallScore(interview.getOverallScore())
                .semanticSimilarity(interview.getSemanticSimilarity())
                .keywordCoverage(interview.getKeywordCoverage())
                .build();
    }
}
