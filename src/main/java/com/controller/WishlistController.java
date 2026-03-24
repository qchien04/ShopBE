package com.controller;

import com.DTO.WishlistDTO;
import com.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistDTO>> getWishlist(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(wishlistService.getWishlist(userId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistDTO> add(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(wishlistService.addToWishlist( productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(
            @PathVariable Long productId,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> check(
            @PathVariable Long productId,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(Map.of("isInWishlist", wishlistService.isInWishlist(userId, productId)));
    }
}
