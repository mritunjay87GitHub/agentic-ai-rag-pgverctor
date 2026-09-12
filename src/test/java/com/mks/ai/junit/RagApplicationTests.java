package com.mks.ai.junit;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Import(TestcontainersConfig.class)
class RagApplicationTests {

    @DynamicPropertySource
    static void aiProps(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.openai.api-key", () -> "test-key");
    }

    @Test
    void contextLoads() {
        // Verifies wiring, Flyway migration and PgVectorStore autoconfig against a real DB.
    }
}
