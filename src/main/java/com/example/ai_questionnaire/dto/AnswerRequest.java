package com.example.ai_questionnaire.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /answer. "answer" is the user's raw natural language reply.
 */
public record AnswerRequest(

		@NotBlank(message = "sessionId is required") String sessionId,

		@NotBlank(message = "answer is required") String answer) {
}
