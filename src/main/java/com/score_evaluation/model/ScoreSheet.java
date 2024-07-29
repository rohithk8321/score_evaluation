package com.score_evaluation.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ScoreSheet {

    @NotNull(message = "testeeId cannot be null")
    private String testeeId;
    private List<Subject> subjects;
}
