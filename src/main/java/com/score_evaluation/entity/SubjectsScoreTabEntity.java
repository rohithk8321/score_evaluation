package com.score_evaluation.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "subjects_score_tab")
public class SubjectsScoreTabEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectScoreId;
    private String subject;
    private Integer totalQuestions;
    private Integer correct;
    private Integer incorrect;
    private Integer unattempted;
    private Double subjectScore;

    @ManyToOne()
    @JoinColumn(name = "testee_id")
    private TesteeTabEntity testeeTab;
}
