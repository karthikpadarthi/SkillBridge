package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.interviewAnswerItem;
import com.PA.BackEnd.dto.interviewEvaluationRequest;
import com.PA.BackEnd.dto.interviewEvaluationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class interviewEvaluationServiceTest {

    private final interviewEvaluationService service = new interviewEvaluationService();

    @Test
    void evaluate_whenAnswersMissing_shouldThrow() {
        interviewEvaluationRequest request = new interviewEvaluationRequest();
        request.setTargetRole("Backend Engineer");
        request.setAnswers(List.of());

        assertThatThrownBy(() -> service.evaluate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("answers are required");
    }

    @Test
    void evaluate_whenStrongAnswerProvided_shouldReturnStructuredFeedback() {
        interviewAnswerItem answer = new interviewAnswerItem();
        answer.setQuestionId("q-1");
        answer.setSkill("SQL");
        answer.setExpectedFocus("query optimization indexing trade-offs reliability");
        answer.setUserAnswer(
                "First, I diagnose latency from slow SQL paths and inspect indexes. " +
                "Second, I propose a trade-off between a composite index and write throughput. " +
                "Finally, I deploy with logging, metrics, and rollback safety because production risk must stay low."
        );

        interviewEvaluationRequest request = new interviewEvaluationRequest();
        request.setTargetRole("Backend Engineer");
        request.setAnswers(List.of(answer));

        interviewEvaluationResponse response = service.evaluate(request);

        assertThat(response.getTargetRole()).isEqualTo("Backend Engineer");
        assertThat(response.getOverallScore()).isBetween(70, 100);
        assertThat(response.getEvaluations()).hasSize(1);
        assertThat(response.getEvaluations().get(0).getQuestionId()).isEqualTo("q-1");
        assertThat(response.getEvaluations().get(0).getStrengths()).isNotEmpty();
        assertThat(response.getNextActions()).anyMatch(action -> action.contains("SQL"));
        assertThat(response.getSummary()).isNotBlank();
    }
}

