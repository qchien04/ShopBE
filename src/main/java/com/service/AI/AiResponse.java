package com.service.AI;

import lombok.Data;

import java.util.List;

@Data
public class AiResponse {
    private String message;
    private List<AiProduct> products;
    private String note;

    @Data
    public static class AiProduct {
        private String name;
        private String reason;
        private Double price;
        private String link;
    }
}
