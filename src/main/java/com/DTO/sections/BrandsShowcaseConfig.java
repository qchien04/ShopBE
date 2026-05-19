package com.DTO.sections;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandsShowcaseConfig {
    private String id;
    private String title;
    private boolean active;
    private List<Long> brandIds;
    private Integer brandCount;
}
