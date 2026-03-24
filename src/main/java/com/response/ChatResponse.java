package com.response;


import java.util.List;

public record ChatResponse(
        List<Candidate> candidates
) {
    public record Candidate(
            Content content
    ) {}

    public record Content(
            List<Part> parts
    ) {}

    public record Part(
            String text
    ) {}
}
