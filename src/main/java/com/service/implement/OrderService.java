package com.service.implement;

import com.DTO.DiscountCalculationResult;
import com.DTO.OrderDTO;
import com.DTO.OrderStatusHistoryDTO;
import com.constant.PaymentMethod;
import com.constant.PaymentStatus;
import com.entity.*;
import com.exception.InvalidRequestException;
import com.exception.NoPermissionException;
import com.exception.NotFoundObjectRequestException;
import com.exception.UserAccountException;
import com.mapper.OrderMapper;
import com.repository.*;
import com.request.AdminOrderFilterRequest;
import com.request.GHNCreateShippingRequest;
import com.request.OrderRequest;
import com.request.UpdateOrderStatusRequest;
import com.response.UserOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final CouponService couponService;
    private final PromotionEngineService promotionEngineService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final GHNService ghnService;

    public List<OrderDTO> getAllOrders() {
        List<Order> list = orderRepository.findAllWithFullInfo();
        return toDetailedDtos(list);
    }

    @Transactional(readOnly = true)
    public UserOrderResponse<OrderDTO> getAllOrdersPaginated(AdminOrderFilterRequest req) {
        Pageable pageable = PageRequest.of(
                req.getPage(), req.getSize(),
                Sort.by(Sort.Direction.DESC, "created_at")
        );

        // Normalize keyword
        String keyword = (req.getKeyword() == null || req.getKeyword().isBlank())
                ? null : req.getKeyword().trim();

        LocalDateTime fromDate = req.getFromDate() != null
                ? req.getFromDate().atStartOfDay() : null;
        LocalDateTime toDate = req.getToDate() != null
                ? req.getToDate().atTime(23, 59, 59) : null;


        String status = req.getStatus();
        String paymentStatus = req.getPaymentStatus();

        if(status.equals("ALL")) status=null;
        if(paymentStatus.equals("ALL")) paymentStatus=null;

        Page<Long> idPage;
        idPage = orderRepository.findIdsByFilters(
                status, keyword, paymentStatus, fromDate, toDate, pageable);

        List<Order> orders = idPage.isEmpty()
                ? List.of()
                : orderRepository.findByIdsWithFullInfo(idPage.getContent());

        List<OrderDTO> dtos = orders.stream().map(this::toDetailedDto).toList();

        List<Object[]> rawCounts = orderRepository.countByStatus();
        Map<String, Long> counts = new HashMap<>();
        long total = 0;

        for (Object[] row : rawCounts) {
            Order.OrderStatus st = (Order.OrderStatus) row[0];
            Long count = (Long) row[1];
            total += count;
            counts.put(st.name(), count);
        }
        counts.put("ALL", total);
        counts.put("PROCESSING", 0L);
        counts.putIfAbsent("PENDING", 0L);
        counts.putIfAbsent("CONFIRMED", 0L);
        counts.putIfAbsent("SHIPPING", 0L);
        counts.putIfAbsent("DELIVERED", 0L);
        counts.putIfAbsent("CANCELLED", 0L);
        counts.putIfAbsent("DELIVERY_FAILED", 0L);
        counts.putIfAbsent("RETURNED", 0L);

        return new UserOrderResponse<>(
                dtos,
                idPage.getNumber(),
                idPage.getSize(),
                idPage.getTotalElements(),
                idPage.getTotalPages(),
                counts
        );
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

    @Transactional(readOnly = true)
    public com.response.UserOrderResponse<OrderDTO> getOrdersByUserIdPaginated(int page, int size, String statusTab) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Order> orderPage;
        if (statusTab == null || "ALL".equalsIgnoreCase(statusTab)) {
            orderPage = orderRepository.findByUserId(myId, pageable);
        } else if ("PROCESSING".equalsIgnoreCase(statusTab)) {
            List<Order.OrderStatus> statuses = List.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.PROCESSING);
            orderPage = orderRepository.findByUserIdAndStatusIn(myId, statuses, pageable);
        } else {
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusTab.toUpperCase());
            orderPage = orderRepository.findByUserIdAndStatus(myId, status, pageable);
        }
        
        Page<OrderDTO> dtoPage = orderPage.map(this::toDetailedDto);
        
        List<Object[]> rawCounts = orderRepository.countByStatusForUser(myId);
        Map<String, Long> counts = new HashMap<>();
        long total = 0;
        long processingCount = 0;
        
        for (Object[] row : rawCounts) {
            Order.OrderStatus status = (Order.OrderStatus) row[0];
            Long count = (Long) row[1];
            total += count;
            
            if (status == Order.OrderStatus.CONFIRMED || status == Order.OrderStatus.PROCESSING) {
                processingCount += count;
            }
            
            counts.put(status.name(), count);
        }
        counts.put("ALL", total);
        counts.put("PROCESSING", processingCount);
        
        counts.putIfAbsent("PENDING", 0L);
        counts.putIfAbsent("SHIPPING", 0L);
        counts.putIfAbsent("DELIVERED", 0L);
        counts.putIfAbsent("CANCELLED", 0L);
        
        return new UserOrderResponse<>(
            dtoPage.getContent(),
            dtoPage.getNumber(),
            dtoPage.getSize(),
            dtoPage.getTotalElements(),
            dtoPage.getTotalPages(),
            counts
        );
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
                .shippingFee(calculateActualShippingFee(customerAddress, products, items))
                .status(Order.OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
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
                            .product(product.getProduct())
                            .attributes(product.getAttributes())
                            .productName(product.getName())
                            .productSlug(product.getProduct().getSlug())
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

        // ── 1. Coupon discount ─────────────────────────────────────────────
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

        // ── 2. Promotion Engine discount ───────────────────────────────────
        List<PromotionEngineService.PromotionContext.CartItemInfo> cartInfoList = newOrder.getItems().stream()
                .map(item -> {
                    ProductVariant variant = item.getProductVariant();
                    Long categoryId = variant.getProduct().getCategory() != null
                            ? variant.getProduct().getCategory().getId()
                            : null;
                    Long brandId = variant.getProduct().getBrand() != null
                            ? variant.getProduct().getBrand().getId()
                            : null;
                    return new PromotionEngineService.PromotionContext.CartItemInfo(
                            variant.getProduct().getId(),
                            variant.getId(),
                            categoryId,
                            brandId,
                            item.getPrice(),
                            item.getQuantity());
                })
                .collect(Collectors.toList());

        PromotionEngineService.PromotionContext promoCtx = new PromotionEngineService.PromotionContext(
                subTotal, newOrder.getShippingFee(), cartInfoList, myId);
        PromotionEngineService.PromotionResult promoResult = promotionEngineService.evaluate(promoCtx);

        double promotionDiscount = promoResult.getTotalDiscount();
        String promotionDetails = promoResult.getAppliedPromotions().isEmpty() ? null
                : promoResult.getAppliedPromotions().stream()
                        .map(PromotionEngineService.AppliedPromotion::description)
                        .collect(Collectors.joining(" | "));

        // ── 4. Merge all discounts ─────────────────────────────────────────
        double totalDiscount = couponDiscount + promotionDiscount;
        String mergedDetails = buildDiscountDetails(couponDetails, promotionDetails);

        newOrder.setDiscount(totalDiscount);
        newOrder.setCouponCode(request.getCouponCode());
        newOrder.setCouponDetails(mergedDetails);
        newOrder.setSubtotal(subTotal);
        double finalTotal = subTotal + newOrder.getShippingFee() - totalDiscount;
        newOrder.setTotal(Math.max(0.0, finalTotal));

        newOrder = orderRepository.save(newOrder);
        logStatusChange(newOrder, null, newOrder.getStatus(), "Đơn hàng được tạo thành công", "USER_ID_" + myId);

        return toDetailedDto(newOrder);
    }

    public DiscountCalculationResult calculateDiscountPreview(OrderRequest request) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        List<OrderRequest.Item> items = request.getItems();
        List<Long> variantIds = items.stream().map(OrderRequest.Item::getProductVariantId).collect(Collectors.toList());
        List<ProductVariant> products = productVariantRepository.findAllById(variantIds);

        double subTotal = 0.0;
        List<PromotionEngineService.PromotionContext.CartItemInfo> cartInfoList = new ArrayList<>();

        for (OrderRequest.Item item : items) {
            for (ProductVariant product : products) {
                if (item.getProductVariantId().equals(product.getId())) {
                    double price = product.getSalePrice();
                    subTotal += price * item.getQuantity();

                    Long categoryId = product.getProduct().getCategory() != null
                            ? product.getProduct().getCategory().getId()
                            : null;
                    Long brandId = product.getProduct().getBrand() != null ? product.getProduct().getBrand().getId()
                            : null;

                    cartInfoList.add(new PromotionEngineService.PromotionContext.CartItemInfo(
                            product.getProduct().getId(),
                            product.getId(),
                            categoryId,
                            brandId,
                            price,
                            item.getQuantity()));
                    break;
                }
            }
        }

        double couponDiscount = 0.0;
        String couponDetails = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            try {
                Coupon coupon = couponService.validateCoupon(request.getCouponCode(), subTotal);
                couponDiscount = couponService.calculateDiscount(coupon, subTotal);
                couponDetails = String.format("Giảm %s%s%s%s",
                        coupon.getDiscountType() == com.entity.Coupon.DiscountType.PERCENTAGE
                                ? coupon.getDiscountValue()
                                : String.format("%,.0f", coupon.getDiscountValue()),
                        coupon.getDiscountType() == com.entity.Coupon.DiscountType.PERCENTAGE ? "%" : "₫",
                        (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount() > 0)
                                ? " (Tối đa " + String.format("%,.0f", coupon.getMaxDiscountAmount()) + "₫)"
                                : "",
                        (coupon.getMinOrderValue() != null && coupon.getMinOrderValue() > 0)
                                ? " - Đơn từ " + String.format("%,.0f", coupon.getMinOrderValue()) + "₫"
                                : "");
            } catch (Exception ignored) {
            }
        }

        CustomerAddress address = customerAddressRepository.findByIdAndUserId(request.getAddressId(), myId).orElse(null);
        double originalShippingFee = 20000;
        if (address != null) {
            originalShippingFee = calculateActualShippingFee(address, products, items);
        }

        PromotionEngineService.PromotionContext promoCtx = new PromotionEngineService.PromotionContext(
                subTotal, originalShippingFee, cartInfoList, myId);
        PromotionEngineService.PromotionResult promoResult = promotionEngineService.evaluate(promoCtx);

        double promotionDiscount = promoResult.getTotalDiscount();
        String promotionDetails = promoResult.getAppliedPromotions().isEmpty() ? null
                : promoResult.getAppliedPromotions().stream()
                        .map(PromotionEngineService.AppliedPromotion::description)
                        .collect(Collectors.joining(" | "));

        double totalDiscount = couponDiscount + promotionDiscount;
        String mergedDetails = buildDiscountDetails(couponDetails, promotionDetails);

        double finalTotal = subTotal + originalShippingFee - totalDiscount;

        return DiscountCalculationResult.builder()
                .subtotal(subTotal)
                .shippingFee(originalShippingFee)
                .couponDiscount(couponDiscount)
                .promotionDiscount(promotionDiscount)
                .totalDiscount(totalDiscount)
                .total(Math.max(0.0, finalTotal))
                .couponDetails(couponDetails)
                .promotionDetails(promotionDetails)
                .mergedDetails(mergedDetails)
                .build();
    }

    private String buildDiscountDetails(String couponDetails, String promotionDetails) {
        if (couponDetails == null && promotionDetails == null)
            return null;
        if (couponDetails == null)
            return promotionDetails;
        if (promotionDetails == null)
            return couponDetails;
        return couponDetails + " | " + promotionDetails;
    }

    private double calculateActualShippingFee(CustomerAddress address, List<ProductVariant> products, List<OrderRequest.Item> requestItems) {
        if (address.getGhnDistrictId() == null || address.getGhnWardCode() == null) {
            return 20000.0; // Fallback if GHN address is not set
        }

        List<Map<String, Object>> ghnItems = new ArrayList<>();
        int totalWeight = 0;
        int maxLength = 10;
        int maxWidth = 10;
        int maxHeight = 10;

        for (OrderRequest.Item item : requestItems) {
            ProductVariant variant = products.stream().filter(p -> p.getId().equals(item.getProductVariantId())).findFirst().orElse(null);
            if (variant != null) {
                Map<String, Object> ghnItem = new HashMap<>();
                ghnItem.put("name", variant.getName() != null ? variant.getName() : "Sản phẩm");
                ghnItem.put("quantity", item.getQuantity());
                ghnItem.put("weight", 200); // Ước tính 200g
                ghnItem.put("length", 10);
                ghnItem.put("width", 10);
                ghnItem.put("height", 5);
                ghnItems.add(ghnItem);
                
                totalWeight += 200 * item.getQuantity();
            }
        }

        try {
            Long insuranceValue = subTotalForInsurance(products, requestItems);
            Map<String, Object> feeResult = ghnService.calculateFee(
                address.getGhnDistrictId(),
                address.getGhnWardCode(),
                totalWeight,
                maxLength,
                maxWidth,
                maxHeight,
                insuranceValue, 
                null,
                ghnItems
            );
            return Double.parseDouble(feeResult.get("total").toString());
        } catch (Exception e) {
            log.warn("Lỗi tính phí giao hàng GHN: {}", e.getMessage());
            return 20000.0;
        }
    }

    private Long subTotalForInsurance(List<ProductVariant> products, List<OrderRequest.Item> requestItems) {
        double subTotal = 0.0;
        for (OrderRequest.Item item : requestItems) {
            ProductVariant variant = products.stream().filter(p -> p.getId().equals(item.getProductVariantId())).findFirst().orElse(null);
            if (variant != null) {
                subTotal += variant.getSalePrice() * item.getQuantity();
            }
        }
        return (long) subTotal;
    }

    public List<Order> findByPaymentStatus(PaymentStatus status) {
        return orderRepository.findByPaymentStatus(status);
    }

    @Transactional
    public Order updatePaymentStatus(Long id, PaymentStatus status, boolean isSystem) {
        if(isSystem){
            Order order = findEntityById(id);
            order.setPaymentStatus(status);
            return orderRepository.save(order);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(auth);
        System.out.println(auth.getAuthorities());
        System.out.println(auth.isAuthenticated());
        if (auth == null || !auth.isAuthenticated()) {
            throw new UserAccountException("Bạn chưa đăng nhập!");
        }

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"));

        if (!isAdmin) {
            throw new NoPermissionException("Bạn không có quyền thực hiện chức năng này!");
        }
        Order order = findEntityById(id);
        order.setPaymentStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public OrderDTO updateOrderPaymentStatusAdmin(Long id, PaymentStatus status) {
        Order order = updatePaymentStatus(id, status, false);
        return toDetailedDto(order);
    }

    @Transactional
    public Order updatePaymentStatus(String orderNumber, PaymentStatus status) {
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

    // ── GHN Shipping Integration ──────────────────────────────────────────────

    /**
     * Tạo đơn vận chuyển GHN cho một đơn hàng đang ở trạng thái PROCESSING.
     * Sau khi GHN tạo thành công:
     *  - Lưu ghnOrderCode vào Order
     *  - Cập nhật shippingFee từ phí GHN trả về
     *  - Chuyển trạng thái đơn hàng sang SHIPPING
     *  - Ghi lịch sử với thông tin ĐVVC/mã vận đơn
     */
    @Transactional
    public Map<String, Object> createGHNShipping(Long orderId, GHNCreateShippingRequest req) {
        Order order = findEntityById(orderId);

        if (order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new IllegalStateException(
                "Chỉ có thể tạo vận đơn GHN cho đơn hàng đang ở trạng thái PROCESSING");
        }

        // Lấy địa chỉ giao hàng từ lịch sử đặt hàng
        // (địa chỉ đã được lưu vào shippingAddress khi tạo đơn)
        // Tìm CustomerAddress có ghnDistrictId/ghnWardCode
        CustomerAddress address = customerAddressRepository
            .findFirstByUserIdAndFullNameAndPhone(
                order.getUser().getId(),
                order.getCustomerName(),
                order.getCustomerPhone())
            .orElse(null);

        Integer toDistrictId = address != null ? address.getGhnDistrictId() : null;
        String toWardCode = address != null ? address.getGhnWardCode() : null;

        if (toDistrictId == null || toWardCode == null) {
            throw new IllegalArgumentException(
                "Địa chỉ giao hàng chưa có thông tin GHN (district/ward code). " +
                "Vui lòng yêu cầu khách cập nhật địa chỉ với thông tin GHN.");
        }

        // Build items list cho GHN
        List<Map<String, Object>> ghnItems = order.getItems().stream()
            .map(item -> {
                Map<String, Object> ghnItem = new HashMap<>();
                ghnItem.put("name", item.getProductName());
                ghnItem.put("code", item.getProductVariant() != null ? item.getProductVariant().getName() : String.valueOf(item.getId()));
                ghnItem.put("quantity", item.getQuantity());
                ghnItem.put("price", item.getPrice() != null ? item.getPrice().longValue() : 0L);
                
                // Kích thước ước tính cho từng sản phẩm
                ghnItem.put("length", 12);
                ghnItem.put("width", 12);
                ghnItem.put("height", 12);
                // Ước tính 200g/item nếu không có dữ liệu cân nặng thực
                ghnItem.put("weight", 200);
                
                ghnItem.put("category", Map.of("level1", "Quần áo")); // Bắt buộc cho một số gói dịch vụ
                return ghnItem;
            })
            .collect(Collectors.toList());

        // Lấy items từ request nếu có (override)
        if (req.getItems() != null && !req.getItems().isEmpty()) {
            ghnItems = req.getItems();
        }

        // Xác định COD: nếu đơn chưa thanh toán và là COD thì thu hộ
        long codAmount = req.getCodAmount() != null ? req.getCodAmount() : 0L;
        if (codAmount == 0 &&
            order.getPaymentStatus() == PaymentStatus.UNPAID &&
            order.getPaymentMethod() == PaymentMethod.COD) {
            codAmount = order.getTotal().longValue();
        }

        GHNService.CreateGHNOrderRequest ghnRequest = GHNService.CreateGHNOrderRequest.builder()
            .toName(order.getCustomerName())
            .toPhone(order.getCustomerPhone())
            .toAddress(order.getShippingAddress())
            .toWardCode(toWardCode)
            .toDistrictId(toDistrictId)
            .weight(req.getWeight())
            .length(req.getLength())
            .width(req.getWidth())
            .height(req.getHeight())
            .serviceTypeId(req.getServiceTypeId())
            .paymentTypeId(req.getPaymentTypeId())
            .requiredNote(req.getRequiredNote())
            .codAmount(codAmount)
            .insuranceValue(req.getInsuranceValue() != null ? req.getInsuranceValue() : order.getTotal().longValue())
            .clientOrderCode(order.getOrderNumber())
            .note(req.getNote())
            .items(ghnItems)
            .build();

        // Gọi GHN API
        Map<String, Object> ghnResult = ghnService.createShippingOrder(ghnRequest);

        String ghnOrderCode = (String) ghnResult.get("order_code");
        Object totalFeeObj = ghnResult.get("total_fee");
        double ghnShippingFee = totalFeeObj != null ? ((Number) totalFeeObj).doubleValue() : order.getShippingFee();

        // Parse expected delivery time
        LocalDateTime expectedDelivery = null;
        try {
            Object expectedObj = ghnResult.get("expected_delivery_time");
            if (expectedObj instanceof String) {
                expectedDelivery = LocalDateTime.parse(
                    ((String) expectedObj).replace(" +0700 +0700", "").trim(),
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            log.warn("Cannot parse GHN expected delivery time: {}", e.getMessage());
        }

        // Cập nhật Order
        order.setGhnOrderCode(ghnOrderCode);
        order.setGhnExpectedDeliveryTime(expectedDelivery);
        
        // Chỉ lưu lại phí thực tế phải trả cho GHN, KHÔNG thay đổi phí khách hàng đã đặt
        order.setActualShippingFee(ghnShippingFee);
        
        order.setStatus(Order.OrderStatus.SHIPPING);
        Order saved = orderRepository.save(order);

        // Ghi lịch sử với thông tin ĐVVC (định dạng cho frontend parse)
        String shippingNote = "ĐVVC: GHN | Shipper: N/A | Mã vận đơn: " + ghnOrderCode;
        logStatusChange(saved, Order.OrderStatus.PROCESSING, Order.OrderStatus.SHIPPING,
            shippingNote, getCurrentUser());

        Map<String, Object> result = new HashMap<>(ghnResult);
        result.put("orderId", orderId);
        result.put("ghnOrderCode", ghnOrderCode);
        result.put("shippingFee", ghnShippingFee);
        return result;
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
                Order.OrderStatus.DELIVERED).contains(status);
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
            product.setSoldCount(
                    Math.max(0, (product.getSoldCount() == null ? 0 : product.getSoldCount()) - item.getQuantity()));
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

    private void logStatusChange(Order order, Order.OrderStatus fromStatus, Order.OrderStatus toStatus, String note,
            String actionBy) {
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
        if (orders == null)
            return new ArrayList<>();
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
