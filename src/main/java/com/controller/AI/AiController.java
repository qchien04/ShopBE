package com.controller.AI;

import com.service.AI.AiResponse;
import com.service.AI.ProductEmbeddingService;
import com.service.AI.ProductRagService;
import com.DTO.ProductDTO;
import com.DTO.ProductEmbeddingProjection;
import com.repository.Ai.ProductEmbeddingRepository;
import com.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final ProductRagService ragService;
    private final ProductEmbeddingService productEmbeddingService;
    private final ProductService productService;
    private final ProductEmbeddingRepository productEmbeddingRepository;

    @PostMapping("/ask")
    public ResponseEntity<AiResponse> ask(@RequestBody AiRequest request) {
        AiResponse res=ragService.ask(request.getQ(), request.getHistory());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    // --- Endpoints cho Python Agent gọi ngược lại ---
    @GetMapping("/internal/addresses")
    public ResponseEntity<?> getAddresses() {
        return ResponseEntity.ok(ragService.getAddressesForCurrentCustomer());
    }

    @GetMapping("/internal/search-products")
    public ResponseEntity<?> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(ragService.searchContextOnly(q));
    }

    @GetMapping("/internal/orders")
    public ResponseEntity<?> getOrders() {
        return ResponseEntity.ok(ragService.getOrdersForCurrentCustomer());
    }

    @GetMapping("/internal/payment-link")
    public ResponseEntity<?> getPaymentLink(@RequestParam Long orderId) {
        return ResponseEntity.ok(ragService.getPaymentLinkForOrder(orderId));
    }

    @PostMapping("/internal/orders")
    public ResponseEntity<?> placeOrder(@RequestBody java.util.Map<String, Object> request) {
        return ResponseEntity.ok(ragService.placeOrder(request));
    }

    @GetMapping("/emb")
    public ResponseEntity<String> ask(@RequestParam Long id) {
        productEmbeddingService.embedAndSave(id);
        return new ResponseEntity<String>("Ok", HttpStatus.OK);
    }

    @GetMapping("/related")
    public ResponseEntity<List<ProductDTO>> getRelatedProducts(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "4") int limit) {
        
        // 1. Generate embedding on-the-fly if missing
        if (!productEmbeddingRepository.existsById(productId)) {
            try {
                productEmbeddingService.embedAndSave(productId);
            } catch (Exception e) {
                System.err.println("Failed to embed product: " + e.getMessage());
            }
        }

        // 2. Query similar product ids
        List<ProductEmbeddingProjection> similarEmbeddings = productEmbeddingRepository.findSimilarProducts(productId, limit);
        
        List<Long> similarIds = similarEmbeddings.stream()
                .map(ProductEmbeddingProjection::getProductId)
                .collect(Collectors.toList());

        // 3. Fallback to same-category products if no embeddings are setup yet
        if (similarIds.isEmpty()) {
            ProductDTO targetProduct = productService.getProductDTOById(productId);
            if (targetProduct.getCategory() != null) {
                List<ProductDTO> fallbacks = productService.getNewProducts(targetProduct.getCategory().getId());
                List<ProductDTO> filtered = fallbacks.stream()
                        .filter(p -> !p.getId().equals(productId))
                        .limit(limit)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(filtered);
            }
            return ResponseEntity.ok(List.of());
        }

        // 4. Load full details
        List<ProductDTO> relatedProducts = productService.getProductsByIds(similarIds);
        return ResponseEntity.ok(relatedProducts);
    }
}
