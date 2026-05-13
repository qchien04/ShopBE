package com.controller;

import com.DTO.PostDTO;
import com.entity.Post;
import com.request.PostRequest;
import com.service.PostService;
import com.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PageResponse<PostDTO>> getAll(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        if (ids != null && !ids.isEmpty()) {
            List<PostDTO> postDTOS = postService.getByIds(ids);
            return ResponseEntity.ok(new PageResponse<>(
                    postDTOS,
                    0,
                    postDTOS.size(),
                    (long) postDTOS.size(),
                    1
            ));
        }

        Page<PostDTO> dtoPage = postService.getAll(page, size, status, category, keyword);
        return ResponseEntity.ok(new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        ));
    }

    @GetMapping("/pretent")
    public ResponseEntity<List<PostDTO>> getPre() {
        return ResponseEntity.ok(postService.getPre());
    }


    @GetMapping("/popular")
    public ResponseEntity<List<PostDTO>> getPopular() {
        return ResponseEntity.ok(postService.getPopular());
    }

    @GetMapping("/rand")
    public ResponseEntity<List<PostDTO>> getRand() {
        return ResponseEntity.ok(postService.getRand());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PostDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PostDTO> create(@Valid @RequestBody PostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest req
    ) {
        return ResponseEntity.ok(postService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PostDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Post.PostStatus status = Post.PostStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(postService.updateStatus(id, status));
    }
}
