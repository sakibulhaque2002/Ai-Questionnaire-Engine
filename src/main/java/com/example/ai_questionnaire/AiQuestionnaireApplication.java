package com.example.ai_questionnaire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiQuestionnaireApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiQuestionnaireApplication.class, args);
	}

}
