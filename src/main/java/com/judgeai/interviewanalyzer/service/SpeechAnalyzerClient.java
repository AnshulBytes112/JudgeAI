package com.judgeai.interviewanalyzer.service;

import com.judgeai.interviewanalyzer.dto.AnalysisResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class SpeechAnalyzerClient {

    private final RestClient restClient;
    private final String analyzerUrl;

    public SpeechAnalyzerClient(RestClient.Builder restClientBuilder, 
                                @Value("${speech-analyzer.url}") String analyzerUrl) {
        this.restClient = restClientBuilder.build();
        this.analyzerUrl = analyzerUrl;
    }

    public AnalysisResultDto analyze(String videoUrl) {
        log.info("Sending video URL to Speech Analyzer: {}", videoUrl);
        try {
            return restClient.post()
                    .uri(analyzerUrl)
                    .body(Map.of("path_or_url", videoUrl))
                    .retrieve()
                    .body(AnalysisResultDto.class);
        } catch (Exception e) {
            log.error("Failed to call speech analyzer", e);
            throw new RuntimeException("Speech analyzer failed", e);
        }
    }
}
