package com.service.AI;

import com.DTO.OrderDTO;
import com.DTO.ProductEmbeddingProjection;
import com.constant.JwtConstant;
import com.entity.ProductVariant;
import com.repository.Ai.ProductEmbeddingRepository;
import com.repository.ProductRepository;
import com.repository.ProductVariantRepository;
import com.service.implement.OrderService;
import com.service.implement.PaymentService;
import com.request.CreatePaymentRequest;
import com.entity.PaymentTransaction;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.repository.CustomerAddressRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductRagService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ProductEmbeddingRepository embeddingRepo;

    @Autowired
    private ProductVariantRepository variantRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private CustomerAddressRepository addressRepo;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private Long getUserIdFromRequest() {
        try {
            HttpServletRequest request = getCurrentRequest();
            String jwt = request.getHeader("Authorization");
            if (jwt == null || !jwt.startsWith("Bearer ")) return null;
            jwt = jwt.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
            return claims.get("id", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    // --- Internal Helpers for Agent ---

    public List<AiResponse.AddressDTO> getAddressesForCurrentCustomer() {
        Long userId = getUserIdFromRequest();
        List<com.entity.CustomerAddress> userAddresses = userId != null ? addressRepo.findByUserId(userId) : List.of();
        List<AiResponse.AddressDTO> addressDTOs = new ArrayList<>();
        for (var addr : userAddresses) {
            AiResponse.AddressDTO dto = new AiResponse.AddressDTO();
            dto.setId(addr.getId());
            dto.setFullName(addr.getFullName());
            dto.setPhone(addr.getPhone());
            dto.setDetailAddress(addr.getDetailAddress());
            dto.setDefault(addr.getIsDefault());
            addressDTOs.add(dto);
        }
        return addressDTOs;
    }

    public List<OrderDTO> getOrdersForCurrentCustomer() {
        Long myId=getUserIdFromRequest();
        return orderService.getOrdersByUserId(myId);
    }

    public Object getPaymentLinkForOrder(Long orderId) {
        CreatePaymentRequest request = new CreatePaymentRequest();
//        request.setOrderId(orderId);
        request.setPaymentMethod(PaymentTransaction.PaymentMethod.BANK_TRANSFER);
        return paymentService.createPaymentLink(getCurrentRequest(), request);
    }

    public List<Map<String, Object>> searchContextOnly(String question) {
        try {
            float[] queryEmbedding = geminiService.embedQuest(question);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < queryEmbedding.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(queryEmbedding[i]);
            }
            sb.append("]");
            String embeddingStr = sb.toString();
            System.out.println(question);
            List<ProductEmbeddingProjection> embeddings = embeddingRepo.searchSimilar(embeddingStr, 5);
            if (embeddings.isEmpty()) return List.of();
            System.out.println("Tim kiem theo query duoc" +embeddings.size());
            for(ProductEmbeddingProjection i:embeddings){
                System.out.println(i.getProductId());
            }
            System.out.println("=========================================");


            return embeddings.stream()
                    .map(e -> productRepo.findById(e.getProductId()).orElse(null))
                    .filter(Objects::nonNull)
                    .map(p -> {
                        List<ProductVariant> variants = variantRepo.findByProductId(p.getId());
                        List<Map<String, Object>> variantMaps = variants.stream().map(v -> {
                            Map<String, Object> vm = new java.util.HashMap<>();
                            vm.put("variantId", v.getId());
                            vm.put("name", v.getName());
                            vm.put("price", v.getPrice());
                            vm.put("stock", v.getStockQuantity());
                            return vm;
                        }).collect(Collectors.toList());

                        Map<String, Object> pm = new java.util.HashMap<>();
                        pm.put("productId", p.getId());
                        pm.put("productName", p.getName());
                        pm.put("price", p.getSalePrice());
                        pm.put("description", p.getShortDescription() != null ? p.getShortDescription() : "");
                        pm.put("imageUrl", p.getMainImage() != null ? p.getMainImage() : "");
                        pm.put("variants", variantMaps);
                        return pm;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public OrderDTO placeOrder(Map<String, Object> agentOrder) {
        try {
            if (agentOrder == null) {
                throw new IllegalArgumentException("Dữ liệu đơn hàng không được để trống");
            }

            Object addressObj = agentOrder.get("addressId");
            if (addressObj == null) {
                throw new IllegalArgumentException("Thiếu addressId");
            }

            Object variantObj = agentOrder.get("variantId");
            if (variantObj == null) {
                throw new IllegalArgumentException("Thiếu variantId");
            }

            Object quantityObj = agentOrder.get("quantity");
            if (quantityObj == null) {
                throw new IllegalArgumentException("Thiếu quantity");
            }

            com.request.OrderRequest request = new com.request.OrderRequest();

            request.setAddressId(Long.valueOf(addressObj.toString()));

            String method = Objects.toString(agentOrder.get("paymentMethod"), "COD");
            request.setPaymentMethod(PaymentTransaction.PaymentMethod.valueOf(method));

            com.request.OrderRequest.Item item = new com.request.OrderRequest.Item();
            item.setProductVariantId(Long.valueOf(variantObj.toString()));
            item.setQuantity(Integer.parseInt(quantityObj.toString()));

            request.setItems(List.of(item));
            Long userId = getUserIdFromRequest();
            return orderService.createOrder(request,userId);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Dữ liệu đặt hàng không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo đơn hàng: " + e.getMessage(), e);
        }
    }

    // --- Main chat with Python Agent ---

    public AiResponse ask(String question, List<Map<String, String>> history) {
        try {
            String authHeader = getCurrentRequest().getHeader("Authorization");
            
            // CHỈ gửi question, history do Agent tự quản lý ở Python side qua cache
            Map<String, Object> agentRequest = Map.of(
                    "question", question
            );

            AiResponse response = webClient.post()
                    .uri("/chat")
                    .header("Authorization", authHeader)
                    .bodyValue(agentRequest)
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block();

            if (response != null) {
                // Hỗ trợ thêm các metadata cần thiết cho FE nếu Agent không trả về đủ
                response.setAvailableAddresses(getAddressesForCurrentCustomer());
                response.setAvailablePaymentMethods(List.of("COD", "BANK_TRANSFER"));
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            AiResponse fallback = new AiResponse();
            fallback.setMessage("AI Agent đang gặp trục trặc... " + e.getMessage());
            return fallback;
        }
    }

    private final org.springframework.web.reactive.function.client.WebClient webClient = org.springframework.web.reactive.function.client.WebClient.create("http://python-agent:8000");
}