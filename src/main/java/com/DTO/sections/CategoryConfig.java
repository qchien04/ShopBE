package com.DTO.sections;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryConfig {
    private String id;
    private String title;
    private boolean active;
    private List<Long> categoryIds;
    private Integer productPerRow;
}
