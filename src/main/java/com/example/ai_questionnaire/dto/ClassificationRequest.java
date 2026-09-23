package com.example.ai_questionnaire.dto;

import java.util.List;
import java.util.Map;

/**
 * Everything sent to the LLM for a single classification call. Deliberately minimal -
 * only the current question, its allowed options (key + context), answers collected so
 * far, and the latest user answer. No conversation history is ever sent.
 */
public record ClassificationRequest(Map<String, Object> answers, String question, List<OptionChoice> allowedOptions,
		String userAnswer) {
}
