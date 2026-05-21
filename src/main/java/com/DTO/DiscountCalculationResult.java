package com.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCalculationResult {
    private double subtotal;
    private double shippingFee;
    private double couponDiscount;
    private double promotionDiscount;
    private double totalDiscount;
    private double total;
    private String couponDetails;
    private String promotionDetails;
    private String mergedDetails;
}
