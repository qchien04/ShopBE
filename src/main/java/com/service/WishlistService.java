package com.service;

import com.DTO.WishlistDTO;
import com.entity.Product;
import com.entity.User;
import com.entity.Wishlist;
import com.repository.ProductRepository;
import com.repository.UserAccountRepo;
import com.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserAccountRepo userRepository;

    public List<WishlistDTO> getWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByAddedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WishlistDTO addToWishlist(Long productId) {
        Long userId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("Sản phẩm đã có trong wishlist");
        }

        User user = userRepository.findByUserIdLong(userId)
                .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại"));

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);

        return toResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new EntityNotFoundException("Không tìm thấy trong wishlist");
        }
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    private WishlistDTO toResponse(Wishlist w) {
        Product p = w.getProduct();
        return new WishlistDTO(
                w.getId(),
                p.getId(),
                p.getName(),
                p.getMainImage(),
                p.getPrice(),
                p.getSalePrice(),
                p.getStatus().name(),
                w.getAddedAt()
        );
    }
}
