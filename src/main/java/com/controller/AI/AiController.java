package com.controller.AI;

import com.service.AI.AiResponse;
import com.service.AI.ProductEmbeddingService;
import com.service.AI.ProductRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final ProductRagService ragService;
    private final ProductEmbeddingService productEmbeddingService;
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
}
