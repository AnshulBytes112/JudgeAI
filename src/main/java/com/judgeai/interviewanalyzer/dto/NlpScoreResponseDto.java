package com.judgeai.interviewanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class NlpScoreResponseDto {
    @JsonProperty("overallScore")
    private Double relevanceScore;
    
    @JsonProperty("semanticSimilarity")
    private Double cosineSimilarity;
    
    @JsonProperty("keywordCoverage")
    private Double keywordCoverage;
    
    @JsonProperty("matchedKeywords")
    private List<String> matchedKeywords;
    
    @JsonProperty("missingKeywords")
    private List<String> missingKeywords;
}
