package com.score_evaluation.controller;

import com.score_evaluation.exception.InvalidRangeException;
import com.score_evaluation.exception.InvalidScoreSheetsException;
import com.score_evaluation.exception.InvalidSubjectException;
import com.score_evaluation.model.ErrorResponse;
import com.score_evaluation.model.ScoreSheet;
import com.score_evaluation.service.ScoreEvaluationService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.score_evaluation.util.EvaluationUtil.validateScoreSheets;
import static com.score_evaluation.util.EvaluationUtil.validateSubjects;

@RestController
@OpenAPIDefinition(info = @Info(
        title = "Score Evaluation",
        description = "Score Sheets Evaluation Spring Boot Application"))
@RequestMapping("/evaluation")
public class ScoreEvaluationController {

    @Autowired
    private ScoreEvaluationService scoreEvaluationService;

    @PostMapping("/sheets")
    @Operation(summary = "Create Testee subject score sheets", description = "Create Testee subject score sheets")
    public ResponseEntity<Object> scoreSheets(@RequestBody List<ScoreSheet> scoreSheets) {
        try {
            validateScoreSheets(scoreSheets);
            return ResponseEntity.accepted().body(scoreEvaluationService.scoreSheets(scoreSheets));
        } catch (InvalidScoreSheetsException | InvalidSubjectException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
        }
    }

    @GetMapping("/scores")
    @Operation(summary = "Retrieve all testees with optional filters", description = "Retrieve all testees with optional filters")
    public ResponseEntity<Object> getScores(@RequestParam(name = "testeeIds", required = false) List<String> testeeIds,
                                             @RequestParam(name = "subjects ", required = false) List<String> subjects,
                                             @RequestParam(name = "totalRange", required = false) String totalRange,
                                             @RequestParam(name = "averageRange", required = false) String averageRange,
                                             @RequestParam(name = "scoreRange", required = false) String scoreRange) {
        try {
            validateSubjects(subjects);
            return ResponseEntity.ok(scoreEvaluationService.getScoreSheets(testeeIds, subjects, totalRange, averageRange, scoreRange));
        } catch (InvalidSubjectException | InvalidRangeException exception) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
        }
    }

}
