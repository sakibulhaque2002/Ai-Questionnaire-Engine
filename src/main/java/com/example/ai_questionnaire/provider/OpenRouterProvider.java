package com.example.ai_questionnaire.provider;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import com.example.ai_questionnaire.dto.ClassificationRequest;
import com.example.ai_questionnaire.dto.ClassificationResult;
import com.example.ai_questionnaire.util.ClassificationPromptFactory;
import com.example.ai_questionnaire.util.ClassificationResponseParser;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Classifies answers using OpenRouter, reached through Spring AI's OpenAI-compatible
 * client pointed at OpenRouter's base URL (see application.properties). JSON output is
 * enforced at the model level through spring.ai.openai.chat.response-format.
 */
@Component
@RequiredArgsConstructor
public class OpenRouterProvider implements AIProvider {

	private final OpenAiChatModel chatModel;

	private final ObjectMapper objectMapper;

	@Override
	public ClassificationResult classify(ClassificationRequest request) {
		try {
			List<Message> messages = ClassificationPromptFactory.buildMessages(objectMapper, request);
			ChatResponse response = chatModel.call(new Prompt(messages));
			String text = response.getResult().getOutput().getText();
			return ClassificationResponseParser.parse(objectMapper, text);
		}
		catch (Exception ex) {
			throw new AIProviderException("openrouter", ex);
		}
	}

}
