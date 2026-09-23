package com.example.ai_questionnaire.util;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import com.example.ai_questionnaire.dto.ClassificationRequest;

import tools.jackson.databind.ObjectMapper;

/**
 * Builds the two-message prompt sent to whichever AIProvider is active. Kept identical
 * across providers so GeminiProvider and OpenRouterProvider only differ in which
 * ChatModel they call.
 */
public final class ClassificationPromptFactory {

	private static final String SYSTEM_PROMPT = """
			You are a strict answer classifier for a questionnaire system.
			You will be given the current question, its allowed options (each with a "key" and its \
			"context" - the content describing what that option means, which may be plain text or \
			HTML), the answers already collected so far, and the user's latest natural language reply.

			The reply may be written in any mix of English, native-script Bengali, or Banglish \
			(Bengali transliterated into Latin letters, e.g. "ami valo achi" for "আমি ভালো আছি" - common \
			informal typing when no Bengali keyboard is available). Options' context may itself be in \
			Bengali script. Interpret the reply's intended meaning regardless of script and match it \
			against the option contexts semantically, not by literal string similarity.

			Decide which single allowed option's context the user's reply corresponds to.

			Rules:
			- Choose only from the allowed options. Never invent a key that is not listed.
			- Judge based on the meaning of the latest answer against each option's context. Use the \
			collected answers only as background context, not as something to classify.
			- "selectedOption" MUST be the matching option's "key" value, exactly as given - never the \
			"context" text itself, even if context is short.
			- If you cannot confidently determine the intended option, set "valid" to false and \
			"selectedOption" to null.
			- Respond with ONLY one JSON object and nothing else: no markdown, no code fences, no \
			explanation.

			Required JSON shape:
			{"valid": true|false, "selectedOption": "<the key of the matching option>"|null, "confidence": 0.0-1.0}
			""";

	private ClassificationPromptFactory() {
	}

	public static List<Message> buildMessages(ObjectMapper objectMapper, ClassificationRequest request) {
		String payload = objectMapper.writeValueAsString(request);
		return List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(payload));
	}

}
