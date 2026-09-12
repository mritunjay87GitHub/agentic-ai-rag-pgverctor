package com.mks.ai.exception;



import java.util.List;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String actual, List<String> allowed) {
        super("Unsupported content type '%s'. Allowed types: %s".formatted(actual, allowed));
    }
}
