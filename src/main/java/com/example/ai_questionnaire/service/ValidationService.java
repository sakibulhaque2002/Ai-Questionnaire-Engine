package com.example.ai_questionnaire.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.ai_questionnaire.model.Option;
import com.example.ai_questionnaire.model.Question;

/**
 * Cheap pre-check performed before ever calling an AI provider. If the user's raw answer
 * already matches an option's context or key (case/whitespace-insensitively), there is
 * no ambiguity to resolve and the AI call can be skipped entirely.
 */
@Service
public class ValidationService {

	public Optional<Option> findExactMatch(Question question, String rawAnswer) {
		String normalized = rawAnswer.strip();
		return question.getOptions().stream().filter(option -> matches(option, normalized)).findFirst();
	}

	private boolean matches(Option option, String normalized) {
		if (option.getContext().equalsIgnoreCase(normalized)) {
			return true;
		}
		Object key = option.getKey();
		return key != null && key.toString().equalsIgnoreCase(normalized);
	}

}
