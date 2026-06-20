package com.judgeai.interviewanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column
    private String videoUrl;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    private Double wpm;

    private Integer fillerWordsCount;

    private Integer pausesCount;

    private Double avgPauseDuration;

    private Double durationSeconds;

    private Double overallScore;

    private Double semanticSimilarity;

    private Double keywordCoverage;
}
