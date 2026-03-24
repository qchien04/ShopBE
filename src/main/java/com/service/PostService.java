package com.service;

import com.DTO.PostDTO;
import com.entity.Post;
import com.exception.NotFoundObjectRequestException;
import com.mapper.PostMapper;
import com.repository.PostRepository;
import com.request.PostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    /** Lấy danh sách bài viết có phân trang + lọc */
    @Transactional(readOnly = true)
    public Page<PostDTO> getAll(int page, int size, String status, String category, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Post.PostStatus postStatus = null;
        if (status != null && !status.isBlank()) {
            postStatus = Post.PostStatus.valueOf(status.toUpperCase());
        }

        if (keyword != null && !keyword.isBlank()) {
            keyword = "%" + keyword.toLowerCase() + "%";
        } else {
            keyword = null;
        }

        return postRepository
                .search(postStatus, category, keyword, pageable)
                .map(postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getPre() {
        Pageable pageable = PageRequest.of(0, 4);
        Page<Post> posts=postRepository.findAll(pageable);

        List<PostDTO> postDTOS=new ArrayList<>();

        for(Post post:posts.stream().toList()){
            post.getTags().size();
            postDTOS.add(postMapper.toResponse(post));
        }
        return postDTOS;
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getPopular() {
        Pageable pageable = PageRequest.of(0, 6);
        Page<Post> posts=postRepository.findAll(pageable);

        List<PostDTO> postDTOS=new ArrayList<>();

        for(Post post:posts.stream().toList()){
            post.getTags().size();
            postDTOS.add(postMapper.toResponse(post));
        }
        return postDTOS;
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getRand() {

        List<Post> posts=postRepository.findRandomPosts();

        List<PostDTO> postDTOS=new ArrayList<>();

        for(Post post:posts){
            post.getTags().size();
            postDTOS.add(postMapper.toResponse(post));
        }
        return postDTOS;
    }


    /** Lấy chi tiết bài viết */
    @Transactional(readOnly = true)
    public PostDTO getById(Long id) {
        Post post = findOrThrow(id);
        post.getTags().size();
        return postMapper.toResponse(post);
    }

    /** Tạo bài viết mới */
    @Transactional
    public PostDTO create(PostRequest req) {
        Post post = postMapper.toEntity(req);
        return postMapper.toResponse(postRepository.save(post));
    }

    /** Cập nhật bài viết */
    @Transactional
    public PostDTO update(Long id, PostRequest req) {
        Post post = findOrThrow(id);
        postMapper.updateEntity(post, req);
        return postMapper.toResponse(postRepository.save(post));
    }

    /** Xóa bài viết */
    @Transactional
    public void delete(Long id) {
        findOrThrow(id);
        postRepository.deleteById(id);
    }

    /** Đổi trạng thái draft ↔ published */
    @Transactional
    public PostDTO updateStatus(Long id, Post.PostStatus status) {
        Post post = findOrThrow(id);
        post.setStatus(status);
        return postMapper.toResponse(postRepository.save(post));
    }

    // -------- private --------
    private Post findOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundObjectRequestException("Không tìm thấy bài viết với id: " + id));
    }
}
