package com.DTO.sections;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotDealsSectionConfig {
    private String id;
    private String title;
    private boolean active;
    private List<Long> productIds;
    private List<Long> weeklyProductIds;
    private String weeklyTitle;
    private Integer productPerRow;
}
