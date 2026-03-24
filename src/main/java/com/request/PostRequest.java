package com.request;

import com.entity.Post;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private String title;
    private String category;
    private String description;
    private String thumbnail;
    private String content;
    private Post.PostStatus status;
    private List<String> tags;
}