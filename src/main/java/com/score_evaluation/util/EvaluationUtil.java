package com.score_evaluation.util;

import com.score_evaluation.exception.InvalidRangeException;
import com.score_evaluation.exception.InvalidSubjectException;
import com.score_evaluation.exception.InvalidScoreSheetsException;
import com.score_evaluation.model.ScoreSheet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.score_evaluation.util.EvaluationConstants.ALLOWED_SUBJECTS;

public class EvaluationUtil {

    public static void validateScoreSheets(List<ScoreSheet> scoreSheets) {
        if (scoreSheets == null || scoreSheets.isEmpty()) {
            throw new InvalidScoreSheetsException("Score Sheets cannot be Null or Empty");
        }
        for (ScoreSheet scoreSheet : scoreSheets) {
            if (scoreSheet.getTesteeId() == null || scoreSheet.getTesteeId().isBlank()) {
                throw new InvalidScoreSheetsException("TesteeId cannot be Null or Empty");
            }
        }
    }

    public static boolean isValidSubject(String subject) {
        return ALLOWED_SUBJECTS.contains(subject);
    }

    public static void validateSubjects(List<String> subjects) {
        if (subjects != null && !subjects.isEmpty()) {
            List<String> invalidSubjects = subjects.stream()
                    .filter(subject -> !isValidSubject(subject))
                    .collect(Collectors.toList());

            if (!invalidSubjects.isEmpty()) {
                throw new InvalidSubjectException("Invalid subjects: " + String.join(", ", invalidSubjects));
            }
        }
    }

    private static final Pattern RANGE_PATTERN = Pattern.compile("^(-?\\d+\\.?\\d*)-(-?\\d+\\.?\\d*)$");

    public static double[] getRange(String range) {
        Matcher matcher = RANGE_PATTERN.matcher(range.trim());
        if (!matcher.matches()) {
            throw new InvalidRangeException("Invalid range format. Expected format: min-max");
        }
        String minStr = matcher.group(1).trim();
        String maxStr = matcher.group(2).trim();
        if (minStr.isEmpty() || maxStr.isEmpty()) {
            throw new InvalidRangeException("Minimum or maximum value cannot be empty");
        }
        double min, max;
        try {
            min = Double.parseDouble(minStr);
            max = Double.parseDouble(maxStr);
        } catch (NumberFormatException e) {
            throw new InvalidRangeException("Invalid number format in range: " + e.getMessage());
        }
        return new double[]{min, max};
    }

    public static double roundScore(double score) {
        BigDecimal averageRounded = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
        return averageRounded.doubleValue();
    }

}
