package com.controller;
import com.DTO.CartDTO;
import com.DTO.CartItemDTO;
import com.entity.CartItem;
import com.request.AddToCartRequest;
import com.request.UpdateCartItemQuantityRequest;
import com.response.ApiResponse;
import com.service.implement.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("")
    public ResponseEntity<CartDTO> getUserCart() {
        return ResponseEntity.ok(cartService.getUserCartDTO());
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(@RequestBody AddToCartRequest request) {
        CartItemDTO item = cartService.addToCart(
                request.getProductVariantId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(item);
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse> updateCartItemQuantity(@PathVariable Long itemId,
                                                       @RequestBody UpdateCartItemQuantityRequest request) {
        cartService.updateCartItemQuantity(itemId, request.getQuantity());
        ApiResponse apiResponse=new ApiResponse("Successfully!",true);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable Long itemId) {
        cartService.removeFromCart(itemId);
        ApiResponse apiResponse=new ApiResponse("Successfully!",true);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart() {
        cartService.clearCart();
        ApiResponse apiResponse=new ApiResponse("Successfully!",true);
        return ResponseEntity.ok(apiResponse);
    }

}