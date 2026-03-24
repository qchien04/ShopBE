package com.response;

import java.util.List;

public record EmbedResponse(
        Embedding embedding
) {
    public record Embedding(
            List<Double> values
    ) {}
}
