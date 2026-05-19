package com.DTO.sections;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedProductConfig {
    private String id;
    private String title;
    private boolean active;
    private List<Long> productIds;
    private Integer productCount;
}
