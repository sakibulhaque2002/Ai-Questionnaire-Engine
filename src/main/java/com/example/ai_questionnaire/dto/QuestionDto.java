package com.example.ai_questionnaire.dto;

import java.util.List;

import com.example.ai_questionnaire.model.Question;

/**
 * Client-facing view of a Question, built from the internal model so the API shape
 * stays decoupled from the selp.json structure.
 */
public record QuestionDto(Integer id, String question, String field, String type, List<OptionDto> options) {

	public static QuestionDto from(Question question) {
		List<OptionDto> options = question.getOptions()
			.stream()
			.map(option -> new OptionDto(option.getContext(), option.getKey()))
			.toList();
		return new QuestionDto(question.getId(), question.getQuestion(), question.getField(), question.getType(),
				options);
	}

}
