package com.example.ai_questionnaire.dto;

/**
 * Response for POST /session.
 */
public record CreateSessionResponse(String sessionId, QuestionDto question) {
}
