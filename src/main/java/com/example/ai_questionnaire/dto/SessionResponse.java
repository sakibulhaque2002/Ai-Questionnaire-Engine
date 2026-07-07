package com.example.ai_questionnaire.dto;

import java.util.Map;

/**
 * Response for GET /session/{id}.
 */
public record SessionResponse(String sessionId, boolean completed, Map<String, Object> answers,
		QuestionDto currentQuestion) {
}
