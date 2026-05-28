package com.controller;

import com.config.GHNConfig;
import com.entity.Order;
import com.entity.CustomerAddress;
import com.repository.OrderRepository;
import com.repository.CustomerAddressRepository;
import com.service.implement.GHNService;
import com.service.implement.GHNService.CreateGHNOrderRequest;
import com.service.implement.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.request.GHNCreateShippingRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ghn")
@RequiredArgsConstructor
public class GHNController {

    private final GHNService ghnService;
    private final GHNConfig ghnConfig;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CustomerAddressRepository customerAddressRepository;

    // ── Master Data ───────────────────────────────────────────────────────────

    @GetMapping("/provinces")
    public ResponseEntity<List<Map<String, Object>>> getProvinces() {
        return ResponseEntity.ok(ghnService.getProvinces());
    }

    @GetMapping("/districts")
    public ResponseEntity<List<Map<String, Object>>> getDistricts(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(ghnService.getDistricts(provinceId));
    }

    @GetMapping("/wards")
    public ResponseEntity<List<Map<String, Object>>> getWards(@RequestParam Integer districtId) {
        return ResponseEntity.ok(ghnService.getWards(districtId));
    }

    // ── Fee Calculation ───────────────────────────────────────────────────────

    @PostMapping("/calculate-fee")
    public ResponseEntity<Map<String, Object>> calculateFee(@RequestBody Map<String, Object> request) {
        Integer toDistrictId = (Integer) request.get("toDistrictId");
        String toWardCode = (String) request.get("toWardCode");
        Integer weight = (Integer) request.get("weight");
        Integer length = (Integer) request.getOrDefault("length", 20);
        Integer width = (Integer) request.getOrDefault("width", 20);
        Integer height = (Integer) request.getOrDefault("height", 10);
        Long insuranceValue = request.get("insuranceValue") != null
                ? ((Number) request.get("insuranceValue")).longValue() : null;
        Integer serviceTypeId = (Integer) request.getOrDefault("serviceTypeId", ghnConfig.getServiceTypeId());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        Map<String, Object> fee = ghnService.calculateFee(
                toDistrictId, toWardCode, weight, length, width, height, insuranceValue, serviceTypeId, items);
        return ResponseEntity.ok(fee);
    }

    @PostMapping("/calculate-fee/{orderId}")
    public ResponseEntity<Map<String, Object>> calculateFeeByOrder(@PathVariable Long orderId, @RequestBody Map<String, Object> request) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        CustomerAddress address = customerAddressRepository
            .findFirstByUserIdAndFullNameAndPhone(
                order.getUser().getId(),
                order.getCustomerName(),
                order.getCustomerPhone())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ có chứa GHN location code"));

        Integer toDistrictId = address.getGhnDistrictId();
        String toWardCode = address.getGhnWardCode();
        if (toDistrictId == null || toWardCode == null) {
            throw new RuntimeException("Địa chỉ khách hàng chưa cập nhật thông tin GHN");
        }

        Integer weight = (Integer) request.get("weight");
        Integer length = (Integer) request.getOrDefault("length", 20);
        Integer width = (Integer) request.getOrDefault("width", 20);
        Integer height = (Integer) request.getOrDefault("height", 10);
        Long insuranceValue = request.get("insuranceValue") != null
                ? ((Number) request.get("insuranceValue")).longValue() : order.getTotal().longValue();
        Integer serviceTypeId = (Integer) request.getOrDefault("serviceTypeId", ghnConfig.getServiceTypeId());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

        Map<String, Object> fee = ghnService.calculateFee(
                toDistrictId, toWardCode, weight, length, width, height, insuranceValue, serviceTypeId, items);
        return ResponseEntity.ok(fee);
    }

    // ── Create Shipping Order ─────────────────────────────────────────────────

    /**
     * Tạo đơn vận chuyển GHN cho một Order có sẵn trong hệ thống.
     * Sau khi tạo thành công, lưu ghnOrderCode vào Order và chuyển trạng thái sang SHIPPING.
     */
    @PostMapping("/create-shipping/{orderId}")
    public ResponseEntity<Map<String, Object>> createShipping(
            @PathVariable Long orderId,
            @RequestBody GHNCreateShippingRequest request) {

        Map<String, Object> result = orderService.createGHNShipping(orderId, request);
        return ResponseEntity.ok(result);
    }

    // ── Tracking ─────────────────────────────────────────────────────────────

    @GetMapping("/tracking/{ghnOrderCode}")
    public ResponseEntity<Map<String, Object>> trackOrder(@PathVariable String ghnOrderCode) {
        return ResponseEntity.ok(ghnService.getShippingOrderDetail(ghnOrderCode));
    }

    // ── Print Label ───────────────────────────────────────────────────────────

    @PostMapping("/print-token")
    public ResponseEntity<Map<String, Object>> generatePrintToken(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> orderCodes = (List<String>) request.get("orderCodes");
        String token = ghnService.generatePrintToken(orderCodes);
        String printUrl = ghnService.getPrintLabelUrl(token);
        return ResponseEntity.ok(Map.of("token", token, "printUrl", printUrl));
    }
}
