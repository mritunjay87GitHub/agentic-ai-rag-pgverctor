package com.mks.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.mks.ai.config.RagProperties;

@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
public class AgenticAiRagPgverctorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgenticAiRagPgverctorApplication.class, args);
	}

}
