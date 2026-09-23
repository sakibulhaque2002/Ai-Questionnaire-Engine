package com.example.ai_questionnaire.dto;

/**
 * Client-facing view of an Option. Deliberately omits nextQuestionId - branching is
 * decided by the backend only and is never exposed to callers.
 */
public record OptionDto(String context, Object key) {
}
