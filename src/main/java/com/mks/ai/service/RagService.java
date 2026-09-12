package com.mks.ai.service;

import com.mks.ai.config.RagProperties;
import com.mks.ai.dto.ChatResponse;
import com.mks.ai.dto.SourceChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final String NO_ANSWER =
            "I could not find the answer to that in the provided documents.";

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final RagProperties props;

    public RagService(ChatClient chatClient, VectorStore vectorStore, RagProperties props) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.props = props;
    }

    public ChatResponse answer(String question) {
        List<Document> retrieved = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(props.topK())
                        .similarityThreshold(props.similarityThreshold())
                        .build());

        log.debug("Retrieved {} chunks for question: {}", retrieved.size(), question);

        // Nothing relevant found -> short-circuit, don't even call the LLM
        if (retrieved.isEmpty()) {
            return new ChatResponse(NO_ANSWER, false, List.of());
        }

        String context = buildContext(retrieved);

        String userMessage = """
                CONTEXT:
                %s

                QUESTION:
                %s
                """.formatted(context, question);

        String answer = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

        boolean grounded = answer != null && !answer.trim().equalsIgnoreCase(NO_ANSWER);
        return new ChatResponse(
                answer == null ? NO_ANSWER : answer.trim(),
                grounded,
                toSources(retrieved));
    }

    private String buildContext(List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < docs.size(); i++) {
            Document d = docs.get(i);
            sb.append("[Chunk ").append(i + 1)
              .append(" | source: ").append(d.getMetadata().getOrDefault("filename", "unknown"))
              .append("]\n")
              .append(d.getText())
              .append("\n\n");
        }
        return sb.toString();
    }

    private List<SourceChunk> toSources(List<Document> docs) {
        return docs.stream().map(d -> new SourceChunk(
                String.valueOf(d.getMetadata().getOrDefault("document_id", "")),
                String.valueOf(d.getMetadata().getOrDefault("filename", "unknown")),
                d.getScore(),
                excerpt(d.getText())
        )).toList();
    }

    private String excerpt(String text) {
        if (text == null) return "";
        return text.length() <= 240 ? text : text.substring(0, 240) + "…";
    }
}

