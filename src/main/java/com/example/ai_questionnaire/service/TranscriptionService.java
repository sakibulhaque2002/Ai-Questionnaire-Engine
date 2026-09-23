package com.example.ai_questionnaire.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import com.example.ai_questionnaire.config.AppProperties;
import com.example.ai_questionnaire.model.Option;
import com.example.ai_questionnaire.model.Question;
import com.example.ai_questionnaire.provider.AIProviderException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Speech-to-text for spoken answers, always via Gemini regardless of app.ai.provider.
 * Only produces text - the transcript is then submitted through the normal answer flow.
 */
@Service
@RequiredArgsConstructor
public class TranscriptionService {

	private static final String SYSTEM_PROMPT = """
			You are a speech-to-text transcriber for a Bangladeshi legal-aid questionnaire.
			Transcribe the attached audio exactly as spoken. The speaker is most likely speaking \
			Bengali, English, or a mix of both.

			Rules:
			- Write Bengali speech in Bengali script and English words in Latin script, as spoken.
			- Do not translate, summarize, correct grammar, or answer the question.
			- The current question and its options (if given) are only hints for recognizing \
			domain words. Never output them unless the speaker actually said them.
			- If there is no intelligible speech, return an empty transcript.
			- Respond with ONLY one JSON object: {"transcript": "<text>"}
			""";

	private final GoogleGenAiChatModel chatModel;

	private final ObjectMapper objectMapper;

	private final AppProperties appProperties;

	public String transcribe(byte[] audio, String mimeType, Question currentQuestion) {
		try {
			UserMessage userMessage = UserMessage.builder()
				.text(buildHint(currentQuestion))
				.media(Media.builder().mimeType(MimeType.valueOf(mimeType)).data(audio).build())
				.build();
			GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
				.model(appProperties.voice().transcriptionModel())
				.temperature(0.0)
				.responseMimeType("application/json")
				.build();
			ChatResponse response = chatModel
				.call(new Prompt(List.of(new SystemMessage(SYSTEM_PROMPT), userMessage), options));
			String text = response.getResult().getOutput().getText();
			TranscriptionResult result = objectMapper.readValue(text, TranscriptionResult.class);
			return result.transcript() == null ? "" : result.transcript().strip();
		}
		catch (Exception ex) {
			throw new AIProviderException("gemini-transcription", ex);
		}
	}

	private String buildHint(Question question) {
		if (question == null) {
			return "Transcribe the attached audio.";
		}
		String options = question.getOptions().stream().map(Option::getContext).collect(Collectors.joining(" | "));
		return "Current question: " + question.getQuestion() + "\nOptions: " + options
				+ "\nTranscribe the attached audio.";
	}

	record TranscriptionResult(String transcript) {
	}

}
