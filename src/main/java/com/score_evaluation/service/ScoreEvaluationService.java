package com.score_evaluation.service;

import com.score_evaluation.model.EvaluatedScoresResponse;
import com.score_evaluation.model.ScoreSheet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScoreEvaluationService {

    public String scoreSheets(List<ScoreSheet> scoreSheets);

    public List<EvaluatedScoresResponse> getScoreSheets(List<String> testeeIds, List<String> subjects, String totalRange, String averageRange, String scoreRange);

}
