package com.mapper;

import com.DTO.PostDTO;
import com.entity.Post;
import com.request.PostRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class PostMapper {

    public Post toEntity(PostRequest req) {
        return Post.builder()
                .title(req.getTitle())
                .category(req.getCategory())
                .description(req.getDescription())
                .thumbnail(req.getThumbnail())
                .tags(req.getTags())
                .content(req.getContent())
                .status(req.getStatus())
                .build();
    }

    public PostDTO toResponse(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .category(post.getCategory())
                .description(post.getDescription())
                .thumbnail(post.getThumbnail())
                .tags(post.getTags())
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    public void updateEntity(Post post, PostRequest req) {
        if (req.getTitle() != null)
            post.setTitle(req.getTitle());
        if (req.getCategory() != null)
            post.setCategory(req.getCategory());
        if (req.getDescription() != null)
            post.setDescription(req.getDescription());
        if (req.getThumbnail() != null)
            post.setThumbnail(req.getThumbnail());
        if (req.getTags() != null)
            post.setTags(req.getTags());
        if (req.getContent() != null)
            post.setContent(req.getContent());
        if (req.getStatus() != null)
            post.setStatus(req.getStatus());
    }
}
