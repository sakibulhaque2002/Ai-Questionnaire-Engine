package com.example.ai_questionnaire.service;

import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.example.ai_questionnaire.dto.OptionChoice;
import com.example.ai_questionnaire.model.Option;
import com.example.ai_questionnaire.model.Question;
import com.example.ai_questionnaire.model.Questionnaire;

import lombok.RequiredArgsConstructor;

/**
 * Read-only access to the loaded Questionnaire: looking up questions and resolving the
 * next question from a chosen option. All branching comes from nextQuestionId in
 * selp.json - there is no if/else on question ids anywhere in this class.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

	private final Questionnaire questionnaire;

	@PostConstruct
	void validateQuestionnaire() {
		for (Question question : questionnaire.getQuestions()) {
			for (Option option : question.getOptions()) {
				Integer nextId = option.getNextQuestionId();
				if (nextId != null && findById(nextId).isEmpty()) {
					throw new IllegalStateException("selp.json is invalid: question " + question.getId()
							+ " option '" + option.getContext() + "' points to unknown nextQuestionId " + nextId);
				}
			}
		}
		if (findById(questionnaire.getStartQuestionId()).isEmpty()) {
			throw new IllegalStateException(
					"selp.json is invalid: startQuestionId " + questionnaire.getStartQuestionId()
							+ " does not match any question");
		}
	}

	public Question getStartQuestion() {
		return getQuestionById(questionnaire.getStartQuestionId());
	}

	public Question getQuestionById(Integer id) {
		return findById(id)
			.orElseThrow(() -> new IllegalStateException("selp.json has no question with id " + id));
	}

	public List<OptionChoice> getAllowedOptionChoices(Question question) {
		return question.getOptions().stream().map(option -> new OptionChoice(option.getKey(), option.getContext())).toList();
	}

	public Optional<Option> findOptionByKey(Question question, String key) {
		return question.getOptions()
			.stream()
			.filter(option -> String.valueOf(option.getKey()).equalsIgnoreCase(key))
			.findFirst();
	}

	/**
	 * @return the next question, or null when the chosen option ends the questionnaire.
	 */
	public Question resolveNextQuestion(Option chosenOption) {
		Integer nextId = chosenOption.getNextQuestionId();
		return nextId == null ? null : getQuestionById(nextId);
	}

	private Optional<Question> findById(Integer id) {
		return questionnaire.getQuestions().stream().filter(q -> q.getId().equals(id)).findFirst();
	}

}
