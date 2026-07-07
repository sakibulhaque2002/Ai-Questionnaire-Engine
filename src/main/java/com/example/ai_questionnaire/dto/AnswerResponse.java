package com.example.ai_questionnaire.dto;

import java.util.Map;

/**
 * Response for POST /answer. nextQuestion is null once completed is true.
 */
public record AnswerResponse(boolean completed, Map<String, Object> answers, QuestionDto nextQuestion) {
}
