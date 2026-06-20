package com.judgeai.interviewanalyzer.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NlpScoreRequestDto {
    private String transcript;
    private String question;
    private String idealAnswer;
    private List<String> keywords;
}
