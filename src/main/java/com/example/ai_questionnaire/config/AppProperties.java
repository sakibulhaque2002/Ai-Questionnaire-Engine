package com.example.ai_questionnaire.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed application configuration bound from the "app.*" prefix in application.properties.
 * {@code ai.provider} is the single switch that selects which {@code AIProvider} handles
 * answer classification - see AIProviderFactory.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Ai ai, Questionnaire questionnaire) {

	public record Ai(String provider) {
	}

	public record Questionnaire(String resource) {
	}

}
