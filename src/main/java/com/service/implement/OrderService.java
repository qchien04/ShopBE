package com.service.implement;

import com.DTO.OrderDTO;
import com.DTO.OrderStatusHistoryDTO;
import com.entity.*;
import com.exception.InvalidRequestException;
import com.exception.NotFoundObjectRequestException;
import com.mapper.OrderMapper;
import com.repository.*;
import com.request.OrderRequest;
import com.request.UpdateOrderStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final CouponService couponService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public List<OrderDTO> getAllOrders() {
        List<Order> list = orderRepository.findAllWithFullInfo();
        return toDetailedDtos(list);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        return toDetailedDto(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        return toDetailedDto(findEntityByNumber(orderNumber));
    }

    private Order findEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    private Order findEntityByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
    }

    public List<OrderDTO> getOrdersByUserId() {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return toDetailedDtos(orderRepository.findFullInfoByUserId(myId));
    }

    public List<OrderDTO> getOrdersByUserId(Long myId) {
        return toDetailedDtos(orderRepository.findFullInfoByUserId(myId));
    }

    @Transactional
    public OrderDTO createOrder(OrderRequest request) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return createOrder(request, myId);
    }

    @Transactional
    public OrderDTO createOrder(OrderRequest request, Long myId) {
        CustomerAddress customerAddress = customerAddressRepository.findByIdAndUserId(request.getAddressId(), myId)
                .orElseThrow(() -> new NotFoundObjectRequestException("Không tồn tại địa chỉ!"));

        List<OrderRequest.Item> items = request.getItems();
        List<Long> productIds = new ArrayList<>();
        for (OrderRequest.Item item : items) {
            productIds.add(item.getProductVariantId());
        }
        List<ProductVariant> products = productVariantRepository.findAllById(productIds);
        List<ProductVariant> updatedProducts = new ArrayList<>();

        String orderNumber = generateOrderNumber();

        Order newOrder = Order.builder()
                .orderNumber(orderNumber)
                .user(User.builder().id(myId).build())
                .customerName(customerAddress.getFullName())
                .customerPhone(customerAddress.getPhone())
                .shippingAddress(customerAddress.getDetailAddress())
                .shippingFee(20000.0)
                .status(Order.OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentTransaction.PaymentStatus.UNPAID)
                .note("Ok")
                .build();

        double subTotal = 0.0;
        newOrder.setItems(new HashSet<>());
        for (OrderRequest.Item item : items) {
            for (ProductVariant product : products) {
                if (item.getProductVariantId().equals(product.getId())) {
                    if (item.getQuantity() > product.getStockQuantity())
                        throw new InvalidRequestException("Hết hàng!");
                    product.setStockQuantity(
                            product.getStockQuantity() - item.getQuantity());
                    double subTotalItem = product.getSalePrice() * item.getQuantity();
                    OrderItem newOrderItem = OrderItem.builder()
                            .order(newOrder)
                            .productVariant(product)
                            .productName(product.getName())
                            .productSku(product.getSku())
                            .quantity(item.getQuantity())
                            .productImage(product.getMainImage())
                            .price(product.getSalePrice())
                            .subtotal(subTotalItem)
                            .build();
                    subTotal += subTotalItem;
                    newOrder.getItems().add(newOrderItem);
                    updatedProducts.add(product);
                    break;
                }
            }
        }
        productVariantRepository.saveAll(updatedProducts);
        updatedProducts.forEach(this::syncProductStock);

        double couponDiscount = 0.0;
        String couponDetails = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponService.validateCoupon(request.getCouponCode(), subTotal);
            couponDiscount = couponService.calculateDiscount(coupon, subTotal);
            couponService.useCoupon(request.getCouponCode());

            couponDetails = String.format("Giảm %s%s%s%s",
                    coupon.getDiscountType() == com.entity.Coupon.DiscountType.PERCENTAGE ? coupon.getDiscountValue()
                            : String.format("%,.0f", coupon.getDiscountValue()),
                    coupon.getDiscountType() == com.entity.Coupon.DiscountType.PERCENTAGE ? "%" : "₫",
                    (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount() > 0)
                            ? " (Tối đa " + String.format("%,.0f", coupon.getMaxDiscountAmount()) + "₫)"
                            : "",
                    (coupon.getMinOrderValue() != null && coupon.getMinOrderValue() > 0)
                            ? " - Đơn từ " + String.format("%,.0f", coupon.getMinOrderValue()) + "₫"
                            : "");
        }

        double totalDiscount = couponDiscount;
        newOrder.setDiscount(totalDiscount);
        newOrder.setCouponCode(request.getCouponCode());
        newOrder.setCouponDetails(couponDetails);
        newOrder.setSubtotal(subTotal);
        double finalTotal = subTotal + newOrder.getShippingFee() - totalDiscount;
        newOrder.setTotal(Math.max(0.0, finalTotal));

        newOrder = orderRepository.save(newOrder);
        logStatusChange(newOrder, null, newOrder.getStatus(), "Đơn hàng được tạo thành công", "USER_ID_" + myId);

        return toDetailedDto(newOrder);
    }

    public List<Order> findByPaymentStatus(PaymentTransaction.PaymentStatus status) {
        return orderRepository.findByPaymentStatus(status);
    }

    @Transactional
    public Order updatePaymentStatus(Long id, PaymentTransaction.PaymentStatus status) {
        Order order = findEntityById(id);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updatePaymentStatus(String orderNumber, PaymentTransaction.PaymentStatus status) {
        Order order = findEntityByNumber(orderNumber);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequest status) {
        Order order = updateStatus(id, status);
        return toDetailedDto(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = findEntityById(id);
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        if (wasStockDeducted(oldStatus)) {
            restoreStock(order);
        }

        logStatusChange(saved, oldStatus, Order.OrderStatus.CANCELLED, "Khách hàng hủy đơn hàng", getCurrentUser());
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }

    // ── Các chuyển trạng thái hợp lệ ─────────────────────────────────────────
    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            Order.OrderStatus.PENDING, Set.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.CONFIRMED, Set.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.PROCESSING, Set.of(Order.OrderStatus.SHIPPING, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.SHIPPING, Set.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.DELIVERY_FAILED),
            Order.OrderStatus.DELIVERY_FAILED, Set.of(Order.OrderStatus.SHIPPING, Order.OrderStatus.RETURNED),
            Order.OrderStatus.DELIVERED, Set.of(Order.OrderStatus.RETURNED),
            Order.OrderStatus.CANCELLED, Set.of(),
            Order.OrderStatus.RETURNED, Set.of());

    // ── Các trạng thái bắt buộc phải có lý do ────────────────────────────────
    private static final Set<Order.OrderStatus> REQUIRE_REASON = Set.of(
            Order.OrderStatus.CANCELLED,
            Order.OrderStatus.DELIVERY_FAILED,
            Order.OrderStatus.RETURNED);

    @Transactional
    public Order updateStatus(Long orderId, UpdateOrderStatusRequest req) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Order.OrderStatus newStatus = req.getStatus();
        Order.OrderStatus oldStatus = order.getStatus();

        // ── 1. Validate chuyển trạng thái ─────────────────────────────────────
        Set<Order.OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(oldStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Không thể chuyển từ " + oldStatus + " sang " + newStatus);
        }

        // ── 2. Validate lý do bắt buộc ────────────────────────────────────────
        if (REQUIRE_REASON.contains(newStatus) &&
                (req.getReason() == null || req.getReason().isBlank())) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập lý do khi chuyển sang trạng thái: " + newStatus);
        }

        // ── 3. Xử lý từng trường hợp ──────────────────────────────────────────
        switch (newStatus) {

            case CONFIRMED -> {
            }

            case PROCESSING -> {
            }

            case DELIVERY_FAILED -> {
                // đếm số lần giao thất bại
                order.setDeliveryAttempts(order.getDeliveryAttempts() + 1);
                order.setCancelReason(req.getReason());
            }

            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                incrementSoldCount(order);
            }

            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason(req.getReason());
                if (wasStockDeducted(oldStatus)) {
                    restoreStock(order);
                }
            }

            case RETURNED -> {
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason(req.getReason());
                restoreStock(order);
                if (oldStatus == Order.OrderStatus.DELIVERED) {
                    decrementSoldCount(order);
                }
            }

            default -> {
            }
        }

        if (req.getInternalNote() != null && !req.getInternalNote().isBlank()) {
            order.setInternalNote(req.getInternalNote());
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        String actionBy = getCurrentUser();
        logStatusChange(savedOrder, oldStatus, newStatus, req.getReason(), actionBy);

        return savedOrder;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Tồn kho đã bị trừ từ trạng thái PENDING trở đi */
    private boolean wasStockDeducted(Order.OrderStatus status) {
        return Set.of(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PROCESSING,
                Order.OrderStatus.SHIPPING,
                Order.OrderStatus.DELIVERY_FAILED,
                Order.OrderStatus.DELIVERED
        ).contains(status);
    }
    private void restoreStock(Order order) {
        order.getItems().forEach(item -> {
            ProductVariant v = item.getProductVariant();
            v.setStockQuantity(v.getStockQuantity() + item.getQuantity());
            productVariantRepository.save(v);
            syncProductStock(v);
        });
    }

    private void incrementSoldCount(Order order) {
        order.getItems().forEach(item -> {
            ProductVariant v = item.getProductVariant();
            v.setSoldCount((v.getSoldCount() == null ? 0 : v.getSoldCount()) + item.getQuantity());
            productVariantRepository.save(v);

            var product = v.getProduct();
            product.setSoldCount((product.getSoldCount() == null ? 0 : product.getSoldCount()) + item.getQuantity());
            productRepository.save(product);
        });
    }

    private void decrementSoldCount(Order order) {
        order.getItems().forEach(item -> {
            ProductVariant v = item.getProductVariant();
            v.setSoldCount(Math.max(0, (v.getSoldCount() == null ? 0 : v.getSoldCount()) - item.getQuantity()));
            productVariantRepository.save(v);

            var product = v.getProduct();
            product.setSoldCount(Math.max(0, (product.getSoldCount() == null ? 0 : product.getSoldCount()) - item.getQuantity()));
            productRepository.save(product);
        });
                    
    }

    /** Đồng bộ stockQuantity tổng lên Product từ tất cả variant */
    private void syncProductStock(ProductVariant changedVariant) {
        var product = changedVariant.getProduct();
        int total = product.getProductVariants().stream()
                .mapToInt(v -> v.getStockQuantity() == null ? 0 : v.getStockQuantity())
                .sum();
        product.setStockQuantity(total);
        productRepository.save(product);
    }

    private void logStatusChange(Order order, Order.OrderStatus fromStatus, Order.OrderStatus toStatus, String note, String actionBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
            
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(note)
                .actionBy(actionBy)
                .build();
        orderStatusHistoryRepository.save(history);
    }

    private OrderDTO toDetailedDto(Order order) {
        OrderDTO dto = orderMapper.toDto(order);
        if (dto != null) {
            dto.setStatusHistory(getHistoryDtos(order.getId()));
        }
        return dto;
    }

    private List<OrderDTO> toDetailedDtos(List<Order> orders) {
        if (orders == null) return new ArrayList<>();
        return orders.stream().map(this::toDetailedDto).toList();
    }
            

    private List<OrderStatusHistoryDTO> getHistoryDtos(Long orderId) {
        return orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(h -> OrderStatusHistoryDTO.builder()
                        .id(h.getId())
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .actionBy(h.getActionBy())
                        .note(h.getNote())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    private String getCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return "USER_ID_" + auth.getPrincipal().toString();
            }
        } catch (Exception e) {
            // ignore
        }
        return "SYSTEM";
    }
}
