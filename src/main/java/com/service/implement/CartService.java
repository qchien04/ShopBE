package com.service.implement;
import com.DTO.CartDTO;
import com.DTO.CartItemDTO;
import com.entity.*;
import com.exception.NotFoundObjectRequestException;
import com.mapper.CartItemMapper;
import com.mapper.CartMapper;
import com.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;


    public CartDTO getUserCartDTO() {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return cartMapper.toDto(cartRepository.findByUserId(myId)
                .orElseGet(() -> createCartForUser(myId)));
    }

    private Cart getUserCart() {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return cartRepository.findByUserId(myId)
                .orElseGet(() -> createCartForUser(myId));
    }

    @Transactional
    public Cart createCartForUser(Long userId) {
        Cart cart = new Cart();
        cart.setUser(new User(userId));
        return cartRepository.save(cart);
    }

    @Transactional
    public CartItemDTO addToCart(Long productVariantId, Integer quantity) {
        Cart cart = getUserCart();
        ProductVariant product = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new NotFoundObjectRequestException("Product not found"));

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), productVariantId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            CartItem cartItem=cartItemRepository.save(item);
            return cartItemMapper.toDto(cartItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductVariant(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getSalePrice() != null ?
                    product.getSalePrice() : product.getPrice());
            CartItem cartItem = cartItemRepository.save(newItem);
            return cartItemMapper.toDto(cartItem);
        }
    }

    @Transactional
    public void updateCartItemQuantity(Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long itemId) {
        CartItem item = cartItemRepository.findById(itemId).orElseThrow();
        item.getCart().getItems().remove(item);
    }

    @Transactional
    public void clearCart() {
        Cart cart = getUserCart();
        cart.getItems().clear();
    }

    public Double getCartTotal(Long userId) {
        Cart cart = getUserCart();
        return cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}

