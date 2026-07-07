package com.example.ai_questionnaire.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_questionnaire.dto.AnswerRequest;
import com.example.ai_questionnaire.dto.AnswerResponse;
import com.example.ai_questionnaire.dto.CreateSessionResponse;
import com.example.ai_questionnaire.dto.ErrorResponse;
import com.example.ai_questionnaire.dto.QuestionDto;
import com.example.ai_questionnaire.dto.SessionResponse;
import com.example.ai_questionnaire.model.Question;
import com.example.ai_questionnaire.model.Session;
import com.example.ai_questionnaire.provider.AIProviderException;
import com.example.ai_questionnaire.service.AnswerNotUnderstoodException;
import com.example.ai_questionnaire.service.QuestionnaireCompletedException;
import com.example.ai_questionnaire.service.SessionNotFoundException;
import com.example.ai_questionnaire.service.SessionService;

import lombok.RequiredArgsConstructor;

/**
 * Thin REST layer: all questionnaire/session/AI logic lives in the service layer. This
 * class only translates between HTTP and the service API, including mapping exceptions
 * to status codes.
 */
@RestController
@RequiredArgsConstructor
public class QuestionnaireController {

	private final SessionService sessionService;

	@PostMapping("/session")
	public CreateSessionResponse createSession() {
		Session session = sessionService.createSession();
		Question question = sessionService.getCurrentQuestion(session);
		return new CreateSessionResponse(session.getSessionId(), QuestionDto.from(question));
	}

	@PostMapping("/answer")
	public AnswerResponse submitAnswer(@Valid @RequestBody AnswerRequest request) {
		Session session = sessionService.submitAnswer(request.sessionId(), request.answer());
		Question next = sessionService.getCurrentQuestion(session);
		return new AnswerResponse(session.isCompleted(), session.getCollectedAnswers(),
				next == null ? null : QuestionDto.from(next));
	}

	@GetMapping("/session/{sessionId}")
	public SessionResponse getSession(@PathVariable String sessionId) {
		Session session = sessionService.getSession(sessionId);
		Question current = sessionService.getCurrentQuestion(session);
		return new SessionResponse(session.getSessionId(), session.isCompleted(), session.getCollectedAnswers(),
				current == null ? null : QuestionDto.from(current));
	}

	@ExceptionHandler(SessionNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(AnswerNotUnderstoodException.class)
	public ResponseEntity<ErrorResponse> handleNotUnderstood(AnswerNotUnderstoodException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(QuestionnaireCompletedException.class)
	public ResponseEntity<ErrorResponse> handleCompleted(QuestionnaireCompletedException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(AIProviderException.class)
	public ResponseEntity<ErrorResponse> handleAIProviderFailure(AIProviderException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.findFirst()
			.orElse("Invalid request");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
	}

}
