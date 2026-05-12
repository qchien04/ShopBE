package com.request;

import com.entity.PaymentTransaction;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long addressId;
    private List<Item> items;
    private PaymentTransaction.PaymentMethod paymentMethod;
    private String couponCode;

    @Data
    public static class Item{
        private Long productVariantId;
        private int quantity;
    }
}