package com.example.ai_questionnaire.dto;

/**
 * One allowed option offered to the AI classifier: its stable key and the context
 * content to judge the user's answer against. The AI must respond with {@code key},
 * never reproduce {@code context} - keeping the match robust even when context is long
 * or contains HTML.
 */
public record OptionChoice(Object key, String context) {
}
