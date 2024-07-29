package com.score_evaluation.repository;

import com.score_evaluation.entity.SubjectsScoreTabEntity;
import com.score_evaluation.entity.TesteeTabEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static com.score_evaluation.util.EvaluationConstants.*;
import static com.score_evaluation.util.EvaluationUtil.*;

@Repository
public class ScoreEvaluationCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TesteeTabEntity> getScoreSheets(
            List<String> testeeIds,
            List<String> subjects,
            String totalRange,
            String averageRange,
            String scoreRange
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TesteeTabEntity> criteriaQuery = criteriaBuilder.createQuery(TesteeTabEntity.class);
        Root<TesteeTabEntity> root = criteriaQuery.from(TesteeTabEntity.class);
        List<Predicate> predicates = new ArrayList<>();
        if(testeeIds != null && !testeeIds.isEmpty()) {
            predicates.add(root.get(TESTEE_ID).in(testeeIds));
        }

        if (subjects != null && !subjects.isEmpty()) {
            for (String subject : subjects) {
                Subquery<Long> subjectSubquery = criteriaQuery.subquery(Long.class);
                Root<TesteeTabEntity> subRoot = subjectSubquery.from(TesteeTabEntity.class);
                Join<TesteeTabEntity, SubjectsScoreTabEntity> subJoin = subRoot.join(SUBJECTSCORETABENTITIES);
                subjectSubquery.select(criteriaBuilder.count(subJoin.get(SUBJECT)));
                subjectSubquery.where(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(subRoot.get(TESTEE_ID), root.get(TESTEE_ID)),
                                criteriaBuilder.equal(subJoin.get(SUBJECT), subject)
                        )
                );
                subjectSubquery.groupBy(subRoot.get(TESTEE_ID));
                subjectSubquery.having(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.count(subJoin.get(SUBJECT)), 1L));
                predicates.add(criteriaBuilder.exists(subjectSubquery));
            }
        }

        if (totalRange != null && !totalRange.isEmpty()) {
            double[] rangeMinMax = getRange(totalRange);
            double min = rangeMinMax[0];
            double max = rangeMinMax[1];
            Subquery<String> totalSubquery = criteriaQuery.subquery(String.class);
            Root<TesteeTabEntity> subRoot = totalSubquery.from(TesteeTabEntity.class);
            Join<TesteeTabEntity, SubjectsScoreTabEntity> subJoin = subRoot.join(SUBJECTSCORETABENTITIES);
            Expression<Double> totalExpression = criteriaBuilder.sum(subJoin.get(SUBJECTSCORE));
            totalSubquery.select(subRoot.get(TESTEE_ID));
            totalSubquery.groupBy(subRoot.get(TESTEE_ID));
            totalSubquery.having(criteriaBuilder.between(totalExpression, min, max));

            predicates.add(criteriaBuilder.in(root.get(TESTEE_ID)).value(totalSubquery));
        }

        if (averageRange != null && !averageRange.isEmpty()) {
            double[] rangeMinMax = getRange(averageRange);
            double min = rangeMinMax[0];
            double max = rangeMinMax[1];
            Subquery<String> averageSubquery = criteriaQuery.subquery(String.class);
            Root<TesteeTabEntity> subRoot = averageSubquery.from(TesteeTabEntity.class);
            Join<TesteeTabEntity, SubjectsScoreTabEntity> subJoin = subRoot.join(SUBJECTSCORETABENTITIES);
            Expression<Double> averageExpression = criteriaBuilder.quot(
                    criteriaBuilder.sum(subJoin.get(SUBJECTSCORE)).as(Double.class), criteriaBuilder.literal(3.0)).as(Double.class);
            averageSubquery.select(subRoot.get(TESTEE_ID));
            averageSubquery.groupBy(subRoot.get(TESTEE_ID));
            averageSubquery.having(criteriaBuilder.between(averageExpression, min, max));

            predicates.add(criteriaBuilder.in(root.get(TESTEE_ID)).value(averageSubquery));
        }

        Subquery<Double> totalScoreSubquery = criteriaQuery.subquery(Double.class);
        Root<TesteeTabEntity> subRoot = totalScoreSubquery.from(TesteeTabEntity.class);
        Join<TesteeTabEntity, SubjectsScoreTabEntity> subJoin = subRoot.join(SUBJECTSCORETABENTITIES);
        Expression<Double> totalScoreExpression = criteriaBuilder.sum(subJoin.get(SUBJECTSCORE));
        totalScoreSubquery.select(totalScoreExpression);
        totalScoreSubquery.groupBy(subRoot.get(TESTEE_ID));
        totalScoreSubquery.where(criteriaBuilder.equal(subRoot.get(TESTEE_ID), root.get(TESTEE_ID)));
        criteriaQuery.orderBy(
                criteriaBuilder.desc(criteriaBuilder.coalesce(totalScoreSubquery, 0.0)),
                criteriaBuilder.asc(root.get(TESTEE_ID))
        );

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        criteriaQuery.select(root).distinct(true);
        TypedQuery<TesteeTabEntity> query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

}
