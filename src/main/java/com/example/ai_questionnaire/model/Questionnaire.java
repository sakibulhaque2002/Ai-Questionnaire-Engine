package com.example.ai_questionnaire.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Root object loaded from selp.json. The whole question flow (including
 * branching) is data-driven from this structure - nothing is hardcoded in Java.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Questionnaire {

	private String questionnaireId;

	private String title;

	private Integer startQuestionId;

	private List<Question> questions;

}
