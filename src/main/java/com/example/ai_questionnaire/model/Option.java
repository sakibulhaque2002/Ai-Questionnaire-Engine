package com.example.ai_questionnaire.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single selectable option for a {@link Question}, as defined in selp.json.
 * {@code context} is the content (plain text or HTML) shown to callers and used by the
 * AI to decide whether a raw answer matches this option. {@code key} is the stable
 * identifier stored as the answer and returned to callers/downstream systems - never
 * the context text. {@code key} is typed as Object because the JSON allows string,
 * boolean or numeric keys (e.g. "option_1" vs true/false).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Option {

	private String context;

	private Object key;

	private Integer nextQuestionId;

}
