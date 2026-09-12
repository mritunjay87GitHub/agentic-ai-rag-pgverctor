package com.mks.ai.junit;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    private void PostgreSQLContaine() {
		// TODO Auto-generated method stub

	}<?> pgvectorContainer() {
        return new PostgreSQLContainer<>(
                DockerImageName.parse("pgvector/pgvector:pg17")
                        .asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("ragdb")
                .withUsername("rag")
                .withPassword("ragpass");
    }
}

