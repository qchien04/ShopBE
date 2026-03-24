package com.request;

import lombok.Data;

@Data
public class CreateRefundRequest {
    private Long transactionId;
    private Double amount;
    private String reason;
}