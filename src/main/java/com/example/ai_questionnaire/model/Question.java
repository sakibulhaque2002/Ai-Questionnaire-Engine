package com.example.ai_questionnaire.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single questionnaire question, as defined in selp.json.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

	private Integer id;

	private String question;

	private String field;

	private String type;

	private List<Option> options;

}
