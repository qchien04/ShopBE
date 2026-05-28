package com.service.implement;

import com.entity.*;
import com.repository.PromotionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.entity.Promotion.PromotionType.*;

/**
 * PromotionEngineService - Trung tâm xử lý tất cả logic khuyến mãi.
 *
 * <p>
 * Luồng hoạt động khi tạo đơn hàng:
 * <ol>
 * <li>Thu thập tất cả promotion đang active</li>
 * <li>Chạy từng promotion engine tương ứng</li>
 * <li>Áp dụng promotion có lợi nhất (hoặc cho phép stack)</li>
 * <li>Trả về PromotionResult</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionEngineService {

    private final PromotionRepository promotionRepository;

    // ══════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════════════════════════════════

    /**
     * Tính toán tất cả khuyến mãi áp dụng cho đơn hàng.
     *
     * @param context Ngữ cảnh đơn hàng (items, user, subtotal,...)
     * @return PromotionResult chứa discount amount và danh sách promotion áp dụng
     */
    public PromotionResult evaluate(PromotionContext context) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findActivePromotions(now);

        List<AppliedPromotion> appliedList = new ArrayList<>();

        for (Promotion promotion : activePromotions) {
            System.out.println(promotion.getDescription());
            Optional<AppliedPromotion> result = applyPromotion(promotion, context);
            result.ifPresent(appliedList::add);
        }

        // Tính tổng discount (không cho phép discount vượt subtotal)
        double totalDiscount = appliedList.stream()
                .mapToDouble(AppliedPromotion::discountAmount)
                .sum();
        totalDiscount = Math.min(totalDiscount, context.getSubtotal()+context.getShippingFee());

        return new PromotionResult(totalDiscount, appliedList);
    }

    /**
     * Lấy giá Flash Sale của một variant sản phẩm (nếu có).
     *
     * @param productId     sản phẩm
     * @param variantId     variant (null để tìm bất kỳ variant)
     * @param originalPrice giá gốc
     * @return giá sau flash sale, hoặc originalPrice nếu không có flash sale
     */
    public double getFlashSalePrice(Long productId, Long variantId, double originalPrice) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> flashSales = promotionRepository.findActiveFlashSales(now);

        for (Promotion p : flashSales) {
            if (p.getFlashSaleItems() == null)
                continue;
            for (Promotion.FlashSaleItem item : p.getFlashSaleItems()) {
                boolean productMatch = Objects.equals(item.getProductId(), productId);
                boolean variantMatch = item.getVariantId() == null
                        || Objects.equals(item.getVariantId(), variantId);
                boolean hasStock = item.getStockLimit() == null
                        || item.getSoldCount() < item.getStockLimit();

                if (productMatch && variantMatch && hasStock) {
                    if (item.getFixedPrice() != null) {
                        return Math.min(item.getFixedPrice(), originalPrice);
                    }
                    if (item.getDiscountPercent() != null) {
                        double discounted = originalPrice * (1 - item.getDiscountPercent() / 100);
                        return Math.max(0, discounted);
                    }
                }
            }
        }
        return originalPrice;
    }

    /**
     * Lấy tất cả Flash Sale đang active kèm thông tin item.
     */
    public List<Promotion> getActiveFlashSales() {
        return promotionRepository.findActiveFlashSales(LocalDateTime.now());
    }

    // ── CRUD ──────────────────────────────────────────────────────────

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAllByOrderByCreatedAtDesc();
    }

    public Promotion getById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found: " + id));
    }

    @Transactional
    public Promotion create(Promotion promotion) {
        if (promotion.getUsedCount() == null)
            promotion.setUsedCount(0);
        if (promotion.getActive() == null)
            promotion.setActive(true);
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion update(Long id, Promotion update) {
        Promotion existing = getById(id);

        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setType(update.getType());
        existing.setStartDate(update.getStartDate());
        existing.setEndDate(update.getEndDate());
        existing.setActive(update.getActive());
        existing.setPriority(update.getPriority());
        existing.setUsageLimit(update.getUsageLimit());
        // Category / Brand
        existing.setApplyCategoryIds(update.getApplyCategoryIds());
        existing.setApplyBrandIds(update.getApplyBrandIds());
        existing.setDiscountValue(update.getDiscountValue());
        existing.setDiscountType(update.getDiscountType());

        return promotionRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    @Transactional
    public Promotion toggleActive(Long id) {
        Promotion p = getById(id);
        p.setActive(!p.getActive());
        return promotionRepository.save(p);
    }

    private Optional<AppliedPromotion> applyPromotion(Promotion promotion, PromotionContext context) {
        return switch (promotion.getType()) {
            case ORDER_TIER -> applyOrderTier(promotion, context);
            case CATEGORY_DISCOUNT -> applyCategoryDiscount(promotion, context);
            case BRAND_DISCOUNT -> applyBrandDiscount(promotion, context);
            case FREE_SHIPPING -> applyFreeShipping(promotion, context);
            default -> Optional.empty();
        };
    }

    /** Giảm giá theo bậc đơn hàng */
    private Optional<AppliedPromotion> applyOrderTier(Promotion p, PromotionContext ctx) {
        if (p.getOrderTiers() == null || p.getOrderTiers().isEmpty())
            return Optional.empty();

        // Tìm bậc phù hợp nhất (min order value <= subtotal, lấy bậc lớn nhất)
        Promotion.OrderTier bestTier = p.getOrderTiers().stream()
                .filter(t -> ctx.getSubtotal() >= t.getMinOrderValue())
                .max(Comparator.comparingDouble(Promotion.OrderTier::getMinOrderValue))
                .orElse(null);

        if (bestTier == null)
            return Optional.empty();

        double discount = calcDiscount(bestTier.getDiscountValue(), bestTier.getDiscountType(),
                ctx.getSubtotal(), bestTier.getMaxDiscountAmount());

        String desc = String.format("Giảm %s (Đơn từ %,.0f₫) - %s",
                formatDiscount(bestTier.getDiscountValue(), bestTier.getDiscountType()),
                bestTier.getMinOrderValue(), p.getName());

        return Optional.of(new AppliedPromotion(p.getId(), p.getName(), desc, discount, p.getType()));
    }

    /** Giảm giá toàn bộ danh mục */
    private Optional<AppliedPromotion> applyCategoryDiscount(Promotion p, PromotionContext ctx) {
        if (p.getApplyCategoryIds() == null || p.getApplyCategoryIds().isEmpty())
            return Optional.empty();
        if (p.getDiscountValue() == null || p.getDiscountType() == null)
            return Optional.empty();

        Set<Long> catSet = new HashSet<>(p.getApplyCategoryIds());
        double categorySubtotal = ctx.getItems().stream()
                .filter(i -> i.categoryId() != null && catSet.contains(i.categoryId()))
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();
        if (categorySubtotal <= 0)
            return Optional.empty();

        double discount = calcDiscount(p.getDiscountValue(), p.getDiscountType(),
                categorySubtotal, null);
        System.out.println(discount);
        String desc = String.format("Giảm %s danh mục - %s",
                formatDiscount(p.getDiscountValue(), p.getDiscountType()), p.getName());

        return Optional.of(new AppliedPromotion(p.getId(), p.getName(), desc, discount, p.getType()));
    }

    /** Giảm giá toàn bộ thương hiệu */
    private Optional<AppliedPromotion> applyBrandDiscount(Promotion p, PromotionContext ctx) {
        if (p.getApplyBrandIds() == null || p.getApplyBrandIds().isEmpty())
            return Optional.empty();
        if (p.getDiscountValue() == null || p.getDiscountType() == null)
            return Optional.empty();

        Set<Long> brandSet = new HashSet<>(p.getApplyBrandIds());
        double brandSubtotal = ctx.getItems().stream()
                .filter(i -> i.brandId() != null && brandSet.contains(i.brandId()))
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();

        if (brandSubtotal <= 0)
            return Optional.empty();

        double discount = calcDiscount(p.getDiscountValue(), p.getDiscountType(),
                brandSubtotal, null);

        String desc = String.format("Giảm %s thương hiệu - %s",
                formatDiscount(p.getDiscountValue(), p.getDiscountType()), p.getName());

        return Optional.of(new AppliedPromotion(p.getId(), p.getName(), desc, discount, p.getType()));
    }

    /** Miễn phí vận chuyển */
    private Optional<AppliedPromotion> applyFreeShipping(Promotion p, PromotionContext ctx) {
        // Kiểm tra min order value (lưu trong orderTiers nếu có)
        if (p.getOrderTiers() != null && !p.getOrderTiers().isEmpty()) {
            double minOrder = p.getOrderTiers().get(0).getMinOrderValue();
            if (ctx.getSubtotal() < minOrder)
                return Optional.empty();
        }

        double discount = ctx.getShippingFee();
        String desc = String.format("Miễn phí vận chuyển - %s", p.getName());

        return Optional.of(new AppliedPromotion(p.getId(), p.getName(), desc, discount, p.getType()));
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private double calcDiscount(Double value, Coupon.DiscountType type, double subtotal, Double maxAmount) {
        if (value == null || type == null)
            return 0.0;
        double discount = type == Coupon.DiscountType.PERCENTAGE
                ? subtotal * (value / 100)
                : value;
        if (maxAmount != null && maxAmount > 0) {
            discount = Math.min(discount, maxAmount);
        }
        return Math.min(discount, subtotal);
    }

    private String formatDiscount(Double value, Coupon.DiscountType type) {
        if (value == null || type == null)
            return "";
        return type == Coupon.DiscountType.PERCENTAGE
                ? value + "%"
                : String.format("%,.0f₫", value);
    }

    // ══════════════════════════════════════════════════════════════════
    // VALUE OBJECTS
    // ══════════════════════════════════════════════════════════════════

    /** Ngữ cảnh đơn hàng truyền vào Promotion Engine */
    @Getter
    @AllArgsConstructor
    public static class PromotionContext {
        private final double subtotal;
        private final double shippingFee;
        private final List<CartItemInfo> items;
        private final Long userId;

        public record CartItemInfo(
                Long productId,
                Long variantId,
                Long categoryId,
                Long brandId,
                double price,
                int quantity) {
        }
    }

    /** Kết quả sau khi tính toán promotion */
    @Getter
    @AllArgsConstructor
    public static class PromotionResult {
        private final double totalDiscount;
        private final List<AppliedPromotion> appliedPromotions;
    }

    /** Một promotion đã được áp dụng */
    public record AppliedPromotion(
            Long promotionId,
            String promotionName,
            String description,
            double discountAmount,
            Promotion.PromotionType type) {
    }
}
