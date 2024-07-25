package com.score_evaluation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/evaluation")
public class ScoreEvaluationController {

    @PostMapping("/sheets")
    public String scoreSheets() {
        return "Hello";
    }

    @GetMapping("/scores")
    public String getScores() {
        return "Hello";
    }

}
