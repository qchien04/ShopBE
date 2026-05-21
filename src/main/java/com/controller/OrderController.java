package com.controller;

import com.DTO.OrderDTO;
import com.entity.PaymentTransaction;
import com.request.OrderRequest;
import com.request.UpdateOrderStatusRequest;
import com.service.implement.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId() {
        return ResponseEntity.ok(orderService.getOrdersByUserId());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/calculate-discount")
    public ResponseEntity<com.DTO.DiscountCalculationResult> calculateDiscountPreview(
            @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.calculateDiscountPreview(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<OrderDTO> updateOrderPaymentStatus(@PathVariable Long id,
            @RequestParam PaymentTransaction.PaymentStatus status) {
        return ResponseEntity.ok(orderService.updateOrderPaymentStatusAdmin(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}
