package com.mks.ai.junit;


import com.mks.ai.config.RagProperties;
import com.mks.ai.dto.ChatResponse;
import com.mks.ai.service.RagService;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RagServiceTest {

    private final RagProperties props =
            new RagProperties(800, 5, 0.55, List.of("text/plain"));

    @Test
    void returnsNoAnswerWhenNoChunksRetrieved() {
        VectorStore store = mock(VectorStore.class);
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        RagService service = new RagService(chatClient, store, props);
        ChatResponse resp = service.answer("What is the capital of Mars?");

        assertThat(resp.grounded()).isFalse();
        assertThat(resp.sources()).isEmpty();
        assertThat(resp.answer()).contains("could not find the answer");
        verifyNoInteractions(chatClient);   // LLM never called when no context
    }

    @Test
    void groundsAnswerInRetrievedContext() {
        VectorStore store = mock(VectorStore.class);
        Document doc = new Document("The Eiffel Tower is 330 metres tall.",
                java.util.Map.of("filename", "facts.txt", "document_id", "abc"));
        when(store.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("It is 330 metres tall.");

        RagService service = new RagService(chatClient, store, props);
        ChatResponse resp = service.answer("How tall is the Eiffel Tower?");

        assertThat(resp.grounded()).isTrue();
        assertThat(resp.answer()).contains("330");
        assertThat(resp.sources()).hasSize(1);
        assertThat(resp.sources().get(0).filename()).isEqualTo("facts.txt");
    }
}

