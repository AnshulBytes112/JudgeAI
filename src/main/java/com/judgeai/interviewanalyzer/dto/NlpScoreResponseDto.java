package com.judgeai.interviewanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class NlpScoreResponseDto {
    @JsonProperty("relevance_score")
    private Double relevanceScore;
    
    @JsonProperty("cosine_similarity")
    private Double cosineSimilarity;
    
    @JsonProperty("keyword_coverage")
    private Double keywordCoverage;
    
    @JsonProperty("matched_keywords")
    private List<String> matchedKeywords;
    
    @JsonProperty("missing_keywords")
    private List<String> missingKeywords;
}
