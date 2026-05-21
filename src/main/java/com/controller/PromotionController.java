package com.controller;

import com.entity.Promotion;
import com.response.ApiResponse;
import com.service.implement.PromotionEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Promotion Engine.
 *
 * Public endpoints (không cần auth):
 *   GET  /promotions/active          - Lấy tất cả promotion đang active
 *   GET  /promotions/flash-sales     - Lấy Flash Sale đang diễn ra
 *   GET  /promotions/{id}            - Chi tiết promotion
 *
 * Admin endpoints (cần auth ADMIN - bảo vệ bởi SecurityConfig):
 *   GET    /admin/promotions         - Danh sách tất cả
 *   POST   /admin/promotions         - Tạo promotion mới
 *   PUT    /admin/promotions/{id}    - Cập nhật
 *   DELETE /admin/promotions/{id}    - Xóa
 *   PATCH  /admin/promotions/{id}/toggle - Bật/Tắt
 */
@RestController
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionEngineService promotionEngineService;

    // ── PUBLIC ENDPOINTS ──────────────────────────────────────────────

    @GetMapping("/promotions/active")
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        return ResponseEntity.ok(promotionEngineService.getActiveFlashSales());
    }

    @GetMapping("/promotions/flash-sales")
    public ResponseEntity<List<Promotion>> getFlashSales() {
        return ResponseEntity.ok(promotionEngineService.getActiveFlashSales());
    }

    @GetMapping("/promotions/{id}")
    public ResponseEntity<Promotion> getPromotion(@PathVariable Long id) {
        return ResponseEntity.ok(promotionEngineService.getById(id));
    }

    // ── ADMIN ENDPOINTS ──────────────────────────────────────────────

    @GetMapping("/admin/promotions")
    public ResponseEntity<List<Promotion>> getAllPromotions() {
        return ResponseEntity.ok(promotionEngineService.getAllPromotions());
    }

    @GetMapping("/admin/promotions/{id}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionEngineService.getById(id));
    }

    @PostMapping("/admin/promotions")
    public ResponseEntity<Promotion> createPromotion(@RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionEngineService.create(promotion));
    }

    @PutMapping("/admin/promotions/{id}")
    public ResponseEntity<Promotion> updatePromotion(
            @PathVariable Long id,
            @RequestBody Promotion promotion) {
        return ResponseEntity.ok(promotionEngineService.update(id, promotion));
    }

    @DeleteMapping("/admin/promotions/{id}")
    public ResponseEntity<ApiResponse> deletePromotion(@PathVariable Long id) {
        promotionEngineService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Xóa promotion thành công!", true));
    }

    @PatchMapping("/admin/promotions/{id}/toggle")
    public ResponseEntity<Promotion> togglePromotion(@PathVariable Long id) {
        return ResponseEntity.ok(promotionEngineService.toggleActive(id));
    }
}
