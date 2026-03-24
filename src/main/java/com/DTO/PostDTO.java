package com.DTO;

import com.entity.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostDTO {
    private Long id;
    private String title;
    private String category;
    private String description;
    private String thumbnail;
    private List<String> tags;
    private String content;
    private Post.PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
