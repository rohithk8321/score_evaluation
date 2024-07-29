package com.score_evaluation.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "testee_tab")
public class TesteeTabEntity {
    @Id
    private String testeeId;

    @OneToMany(mappedBy = "testeeTab", cascade = CascadeType.ALL)
    private List<SubjectsScoreTabEntity> subjectsScoreTabEntities;
}
