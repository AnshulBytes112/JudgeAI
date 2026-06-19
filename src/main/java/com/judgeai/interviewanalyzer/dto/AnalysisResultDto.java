package com.judgeai.interviewanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnalysisResultDto {
    private String transcript;
    private Double wpm;
    
    @JsonProperty("filler_words")
    private FillerWordsDto fillerWords;
    
    private PausesDto pauses;
    
    @JsonProperty("duration_seconds")
    private Double durationSeconds;

    @Data
    public static class FillerWordsDto {
        private Integer total;
    }

    @Data
    public static class PausesDto {
        private Integer count;
        @JsonProperty("avg_duration")
        private Double avgDuration;
    }
}
