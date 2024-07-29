package com.score_evaluation.model;

import lombok.Data;

@Data
public class Subject {
    private String subject;
    private Integer totalQuestions;
    private Integer correct;
    private Integer incorrect;
}
