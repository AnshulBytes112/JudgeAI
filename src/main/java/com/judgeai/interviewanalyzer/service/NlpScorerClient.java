package com.judgeai.interviewanalyzer.service;

import com.judgeai.interviewanalyzer.dto.NlpScoreRequestDto;
import com.judgeai.interviewanalyzer.dto.NlpScoreResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class NlpScorerClient {

    private final RestClient restClient;

    public NlpScorerClient(@Value("${nlp-scorer.url:http://localhost:8001/analyze-content}") String scorerUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(scorerUrl)
                .build();
    }

    public NlpScoreResponseDto scoreContent(NlpScoreRequestDto request) {
        log.info("Sending transcript to NLP Scorer for evaluation...");
        return restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(NlpScoreResponseDto.class);
    }
}
