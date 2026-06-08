package com.request;

import com.constant.PaymentMethod;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long addressId;
    private List<Item> items;
    private PaymentMethod paymentMethod;
    private String couponCode;
    private String note;

    @Data
    public static class Item{
        private Long productVariantId;
        private int quantity;
    }
}