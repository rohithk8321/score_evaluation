package com.score_evaluation.model;

import lombok.Data;

import java.util.Map;

@Data
public class EvaluatedScoresResponse {
    private String testeeId;
    private Map<String, Double> scores;
}
