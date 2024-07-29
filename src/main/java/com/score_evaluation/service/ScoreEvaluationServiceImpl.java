package com.score_evaluation.service;

import com.score_evaluation.entity.SubjectsScoreTabEntity;
import com.score_evaluation.entity.TesteeTabEntity;
import com.score_evaluation.exception.InvalidSubjectException;
import com.score_evaluation.model.EvaluatedScoresResponse;
import com.score_evaluation.model.ScoreSheet;
import com.score_evaluation.model.Subject;
import com.score_evaluation.repository.ScoreEvaluationCriteriaRepository;
import com.score_evaluation.repository.ScoreEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.score_evaluation.util.EvaluationUtil.isValidSubject;
import static com.score_evaluation.util.EvaluationUtil.roundScore;

@Service
public class ScoreEvaluationServiceImpl implements ScoreEvaluationService {

    @Autowired
    private ScoreEvaluationRepository scoreEvaluationRepository;

    @Autowired
    private ScoreEvaluationCriteriaRepository evaluationCriteriaRepository;

    @Transactional
    public String scoreSheets(List<ScoreSheet> scoreSheets) {
        List<TesteeTabEntity> testeeTabEntities = new ArrayList<>();
        for (ScoreSheet scoreSheet : scoreSheets) {
            TesteeTabEntity testeeTabEntity = new TesteeTabEntity();
            testeeTabEntity.setTesteeId(scoreSheet.getTesteeId());
            if (scoreSheet.getSubjects() != null && !scoreSheet.getSubjects().isEmpty()) {
                scoreSheet.getSubjects().forEach(subject -> {
                    if (subject.getSubject() == null || subject.getSubject().isEmpty() ||
                            !isValidSubject(subject.getSubject())) {
                        throw new InvalidSubjectException("Invalid Subject " + subject.getSubject());
                    }
                    if (subject.getTotalQuestions() == null || subject.getIncorrect() == null ||
                            subject.getCorrect() == null) {
                        throw new InvalidSubjectException("Invalid scores for subject " + subject.getSubject());
                    }
                    if (subject.getCorrect() + subject.getIncorrect() > subject.getTotalQuestions()) {
                        throw new InvalidSubjectException("Total of correct and incorrect answers exceeds total questions for subject " + subject.getSubject());
                    }
                });
                List<SubjectsScoreTabEntity> subjectsScoreTabEntities = scoreSheet.getSubjects().stream()
                        .filter(subject -> subject.getSubject() != null && !subject.getSubject().isEmpty())
                        .filter(subject -> subject.getTotalQuestions() != null && subject.getIncorrect() != null && subject.getCorrect() != null)
                        .map(this::buildSubjectsScoreTabEntity)
                        .collect(Collectors.toList());
                subjectsScoreTabEntities.forEach(subjectsScoreTabEntity -> subjectsScoreTabEntity.setTesteeTab(testeeTabEntity));
                testeeTabEntity.setSubjectsScoreTabEntities(subjectsScoreTabEntities);
                testeeTabEntities.add(testeeTabEntity);
            }
        }
        saveOrUpdateTestees(testeeTabEntities);
        return "Score Sheets Created";
    }

    private void saveOrUpdateTestees(List<TesteeTabEntity> testeeTabEntities) {
        for (TesteeTabEntity newTestee : testeeTabEntities) {
            scoreEvaluationRepository.findById(newTestee.getTesteeId()).ifPresentOrElse(
                    existingTestee -> updateExistingTestee(existingTestee, newTestee),
                    () -> scoreEvaluationRepository.save(newTestee));
        }
    }

    private void updateExistingTestee(TesteeTabEntity existingTestee, TesteeTabEntity newTestee) {
        List<SubjectsScoreTabEntity> existingSubjects = existingTestee.getSubjectsScoreTabEntities();
        List<SubjectsScoreTabEntity> newSubjects = newTestee.getSubjectsScoreTabEntities();
        for (SubjectsScoreTabEntity newSubject : newSubjects) {
            SubjectsScoreTabEntity existingSubject = findExistingSubject(existingSubjects, newSubject);
            if (existingSubject != null) {
                updateExistingSubject(existingSubject, newSubject);
            } else {
                newSubject.setTesteeTab(existingTestee);
                existingSubjects.add(newSubject);
            }
        }
        scoreEvaluationRepository.save(existingTestee);
    }

    private SubjectsScoreTabEntity findExistingSubject(List<SubjectsScoreTabEntity> existingSubjects, SubjectsScoreTabEntity newSubject) {
        return existingSubjects.stream()
                .filter(existingSubject -> existingSubject.getSubject().equals(newSubject.getSubject()))
                .findFirst()
                .orElse(null);
    }

    private void updateExistingSubject(SubjectsScoreTabEntity existingSubject, SubjectsScoreTabEntity newSubject) {
        existingSubject.setTotalQuestions(newSubject.getTotalQuestions());
        existingSubject.setCorrect(newSubject.getCorrect());
        existingSubject.setIncorrect(newSubject.getIncorrect());
        existingSubject.setUnattempted(newSubject.getUnattempted());
        existingSubject.setSubjectScore(newSubject.getSubjectScore());
    }

    private SubjectsScoreTabEntity buildSubjectsScoreTabEntity(Subject subject) {
        SubjectsScoreTabEntity subjectsScoreTabEntity = new SubjectsScoreTabEntity();
        subjectsScoreTabEntity.setSubject(subject.getSubject());
        subjectsScoreTabEntity.setTotalQuestions(subject.getTotalQuestions());
        subjectsScoreTabEntity.setCorrect(subject.getCorrect());
        subjectsScoreTabEntity.setIncorrect(subject.getIncorrect());
        subjectsScoreTabEntity.setSubjectScore((subject.getCorrect() - (subject.getIncorrect() * 0.25)));
        subjectsScoreTabEntity.setUnattempted(subject.getTotalQuestions() - (subject.getCorrect() + subject.getIncorrect()));
        return subjectsScoreTabEntity;
    }

    public List<EvaluatedScoresResponse> getScoreSheets(List<String> testeeIds,
                                                        List<String> subjects,
                                                        String totalRange,
                                                        String averageRange,
                                                        String scoreRange) {
        List<TesteeTabEntity> testeeTabEntities = evaluationCriteriaRepository.getScoreSheets(testeeIds, subjects, totalRange, averageRange, scoreRange);
        return testeeTabEntities.stream()
                .map(this::getEvaluatedScoresResponse)
                .collect(Collectors.toList());
    }

    private EvaluatedScoresResponse getEvaluatedScoresResponse(TesteeTabEntity testeeTabEntity) {
        EvaluatedScoresResponse evaluatedScoresResponse = new EvaluatedScoresResponse();
        evaluatedScoresResponse.setTesteeId(testeeTabEntity.getTesteeId());
        Map<String, Double> map = new HashMap<>();
        double total = 0.0;
        for (SubjectsScoreTabEntity subjectsScoreTabEntity : testeeTabEntity.getSubjectsScoreTabEntities()
        ) {
            map.put(subjectsScoreTabEntity.getSubject(), subjectsScoreTabEntity.getSubjectScore());
            total = total + subjectsScoreTabEntity.getSubjectScore();
        }
        map.put("total", total);
        double average = total == 0.0 ? 0.0 : total / map.size();
        map.put("average", roundScore(average));
        evaluatedScoresResponse.setScores(map);
        return evaluatedScoresResponse;
    }

}
