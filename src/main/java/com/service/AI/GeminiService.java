package com.service.AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${cohere.api-key}")
    private String cohereApiKey;

    private final WebClient client = WebClient.create();


    public float[] embed(String text) {

        Map<String, Object> body = Map.of(
                "model", "embed-multilingual-v3.0",
                "texts", List.of(text),
                "input_type", "search_document"
        );

        // DEBUG REQUEST
        System.out.println("=== COHERE EMBED REQUEST ===");
        System.out.println("URL: https://api.cohere.com/v1/embed");
        System.out.println("API Key: ***" + cohereApiKey.substring(cohereApiKey.length() - 4));
        System.out.println("Body: " + body);
        System.out.println("============================");

        Map<String, Object> res;

        try {
            res = client.post()
                    .uri("https://api.cohere.com/v1/embed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cohereApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.out.println("=== COHERE ERROR ===");
                                        System.out.println("Status: " + response.statusCode());
                                        System.out.println("Body: " + errorBody);
                                        System.out.println("===================");
                                        return Mono.error(new RuntimeException(errorBody));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("=== COHERE RESPONSE ===");
            System.out.println(res);
            System.out.println("======================");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // =========================
        // PARSE RESPONSE
        // {
        //   "embeddings": [[...]]
        // }
        // =========================
        List<List<Double>> embeddings =
                (List<List<Double>>) res.get("embeddings");

        if (embeddings == null || embeddings.isEmpty()) {
            throw new RuntimeException("No embeddings returned from Cohere");
        }

        List<Double> values = embeddings.get(0);

        System.out.println("Vector size: " + values.size());
        System.out.println("First 5 values: " + values.subList(0, Math.min(5, values.size())));

        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i).floatValue();
        }

        return vector;
    }


    public float[] embedQuest(String text) {

        Map<String, Object> body = Map.of(
                "model", "embed-multilingual-v3.0",
                "texts", List.of(text),
                "input_type", "search_query"
        );

        // DEBUG REQUEST
        System.out.println("=== COHERE EMBED REQUEST ===");
        System.out.println("URL: https://api.cohere.com/v1/embed");
        System.out.println("API Key: ***" + cohereApiKey.substring(cohereApiKey.length() - 4));
        System.out.println("Body: " + body);
        System.out.println("============================");

        Map<String, Object> res;

        try {
            res = client.post()
                    .uri("https://api.cohere.com/v1/embed")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cohereApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.out.println("=== COHERE ERROR ===");
                                        System.out.println("Status: " + response.statusCode());
                                        System.out.println("Body: " + errorBody);
                                        System.out.println("===================");
                                        return Mono.error(new RuntimeException(errorBody));
                                    })
                    )
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("=== COHERE RESPONSE ===");
            System.out.println(res);
            System.out.println("======================");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // =========================
        // PARSE RESPONSE
        // {
        //   "embeddings": [[...]]
        // }
        // =========================
        List<List<Double>> embeddings =
                (List<List<Double>>) res.get("embeddings");

        if (embeddings == null || embeddings.isEmpty()) {
            throw new RuntimeException("No embeddings returned from Cohere");
        }

        List<Double> values = embeddings.get(0);

        System.out.println("Vector size: " + values.size());
        System.out.println("First 5 values: " + values.subList(0, Math.min(5, values.size())));

        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i).floatValue();
        }

        return vector;
    }

//    public float[] embed(String text) {
//        Map<String, Object> body = Map.of(
//                "model", "models/text-embedding-004",
//                "content", Map.of(
//                        "parts", List.of(
//                                Map.of("text", text)
//                        )
//                )
//        );
//
//        // Debug: In ra request body
//        System.out.println("=== DEBUG REQUEST ===");
//        System.out.println("URL: https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent");
//        System.out.println("API Key: " + (apiKey != null ? "***" + apiKey.substring(Math.max(0, apiKey.length() - 4)) : "NULL"));
//        System.out.println("Request Body: " + body);
//        System.out.println("====================");
//
//        Map<String, Object> res = null;
//        try {
//            res = client.post()
//                    .uri("https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=" + apiKey)
//                    .header("Content-Type", "application/json")
//                    .bodyValue(body)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::isError, response -> {
//                        return response.bodyToMono(String.class)
//                                .flatMap(errorBody -> {
//                                    System.out.println("=== DEBUG ERROR RESPONSE ===");
//                                    System.out.println("Status Code: " + response.statusCode());
//                                    System.out.println("Headers: " + response.headers().asHttpHeaders());
//                                    System.out.println("Error Body: " + errorBody);
//                                    System.out.println("============================");
//                                    return Mono.error(new RuntimeException("API Error: " + errorBody));
//                                });
//                    })
//                    .bodyToMono(Map.class)
//                    .block();
//
//            // Debug: In ra response
//            System.out.println("=== DEBUG SUCCESS RESPONSE ===");
//            System.out.println("Response: " + res);
//            System.out.println("==============================");
//
//        } catch (Exception e) {
//            System.out.println("=== DEBUG EXCEPTION ===");
//            System.out.println("Exception Type: " + e.getClass().getName());
//            System.out.println("Exception Message: " + e.getMessage());
//            e.printStackTrace();
//            System.out.println("=======================");
//            throw e;
//        }
//
//        // Parse response - thử cả 2 cấu trúc có thể
//        List<Double> values = null;
//
//        // Thử cấu trúc 1: { "embedding": { "values": [...] } }
//        if (res.containsKey("embedding")) {
//            System.out.println("Using 'embedding' structure");
//            Map<String, Object> embedding = (Map<String, Object>) res.get("embedding");
//            values = (List<Double>) embedding.get("values");
//        }
//        // Thử cấu trúc 2: { "embeddings": [ { "values": [...] } ] }
//        else if (res.containsKey("embeddings")) {
//            System.out.println("Using 'embeddings' structure");
//            List<Map<String, Object>> embeddings = (List<Map<String, Object>>) res.get("embeddings");
//            values = (List<Double>) embeddings.get(0).get("values");
//        }
//        else {
//            System.out.println("Unknown response structure. Keys: " + res.keySet());
//            throw new RuntimeException("Cannot parse embedding from response");
//        }
//
//        System.out.println("=== DEBUG PARSED VALUES ===");
//        System.out.println("Vector size: " + values.size());
//        System.out.println("First 5 values: " + values.subList(0, Math.min(5, values.size())));
//        System.out.println("===========================");
//
//        float[] vector = new float[values.size()];
//        for (int i = 0; i < values.size(); i++) {
//            vector[i] = values.get(i).floatValue();
//        }
//
//        return vector;
//    }



    public String chat(String prompt) {

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        Map<String, Object> res = client.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) res.get("candidates");

        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

        return (String) parts.get(0).get("text");
    }

}

