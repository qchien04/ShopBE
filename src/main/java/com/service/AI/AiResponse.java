package com.service.AI;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiResponse {
    private String message;
    private List<AiProduct> products;
    private String note;
    private Action action;
    private List<AddressDTO> availableAddresses;
    private List<String> availablePaymentMethods;

    @Data
    public static class AiProduct {
        private Long productId;
        private String productName;
        private Double price;
        private String description;
        private String imageUrl;
        private List<AiVariant> variants;
    }

    @Data
    public static class AiVariant {
        private Long variantId;
        private String name;
        private Double price;
        private Integer stock;
    }

    @Data
    public static class Action {
        private String type; // e.g., "ORDER", "VIEW_CART"
        private Map<String, Object> params;
    }

    @Data
    public static class AddressDTO {
        private Long id;
        private String fullName;
        private String phone;
        private String detailAddress;
        private boolean isDefault;
    }
}
