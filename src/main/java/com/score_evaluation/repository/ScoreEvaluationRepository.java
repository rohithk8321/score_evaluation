package com.score_evaluation.repository;

import com.score_evaluation.entity.TesteeTabEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreEvaluationRepository extends JpaRepository<TesteeTabEntity, String> {
}
