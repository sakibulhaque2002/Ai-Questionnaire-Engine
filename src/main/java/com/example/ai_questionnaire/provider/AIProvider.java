package com.example.ai_questionnaire.provider;

import com.example.ai_questionnaire.dto.ClassificationRequest;
import com.example.ai_questionnaire.dto.ClassificationResult;

/**
 * A single LLM backend capable of classifying a user's natural language answer into one
 * of a question's allowed options. Implementations only do classification - the
 * questionnaire flow itself is controlled entirely by the backend.
 */
public interface AIProvider {

	ClassificationResult classify(ClassificationRequest request);

}
