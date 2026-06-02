package com.controller;

import com.entity.FAQ;
import com.repository.FAQRepository;
import com.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@RestController
@RequestMapping("/faqs")
@RequiredArgsConstructor
public class FAQController {

    private final FAQRepository faqRepository;

    // ── Request DTO (inner record) ──────────────────────────
    public record FAQRequest(
            @NotBlank(message = "Danh mục không được để trống")
            @Size(max = 100)
            String category,

            @NotBlank(message = "Câu hỏi không được để trống")
            @Size(max = 500)
            String question,

            @NotBlank(message = "Câu trả lời không được để trống")
            String answer,

            Integer displayOrder
    ) {}

    // ── PUBLIC: Lấy tất cả FAQ (cho trang client) ───────────
    @GetMapping
    public ResponseEntity<List<FAQ>> getAll(
            @RequestParam(required = false) String category
    ) {
        List<FAQ> result = category != null && !category.isBlank()
                ? faqRepository.findByCategoryIgnoreCaseOrderByDisplayOrderAsc(category)
                : faqRepository.findAllByOrderByDisplayOrderAscCreatedAtAsc();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FAQ> getById(@PathVariable Long id) {
        return faqRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── ADMIN: Tạo mới ──────────────────────────────────────
    @PostMapping("/admin")
    public ResponseEntity<FAQ> create(@Valid @RequestBody FAQRequest req) {
        FAQ faq = FAQ.builder()
                .category(req.category())
                .question(req.question())
                .answer(req.answer())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(faqRepository.save(faq));
    }

    // ── ADMIN: Cập nhật ─────────────────────────────────────
    @PutMapping("/admin/{id}")
    public ResponseEntity<FAQ> update(
            @PathVariable Long id,
            @Valid @RequestBody FAQRequest req
    ) {
        return faqRepository.findById(id).map(existing -> {
            existing.setCategory(req.category());
            existing.setQuestion(req.question());
            existing.setAnswer(req.answer());
            if (req.displayOrder() != null) existing.setDisplayOrder(req.displayOrder());
            return ResponseEntity.ok(faqRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── ADMIN: Xóa ──────────────────────────────────────────
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        if (!faqRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        faqRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse("Xóa câu hỏi thành công!", true));
    }
}
