package com.service.implement;

import com.DTO.ShippingConfigDTO;
import com.config.GHNConfig;
import com.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GHNService {

    private final GHNConfig ghnConfig;
    private final RestTemplate restTemplate;
    private final ConfigService configService;

    // ── Base headers ─────────────────────────────────────────────────────────

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnConfig.getToken());
        return headers;
    }

    private HttpHeaders buildHeadersWithShop() {
        HttpHeaders headers = buildHeaders();
        headers.set("ShopId", String.valueOf(ghnConfig.getShopId()));
        return headers;
    }

    // ── Master Data APIs ─────────────────────────────────────────────────────

    /** Lấy danh sách tỉnh/thành phố */
    public List<Map<String, Object>> getProvinces() {
        String url = ghnConfig.getApiUrl() + "/shiip/public-api/master-data/province";
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
        return extractDataList(response.getBody());
    }

    /** Lấy danh sách quận/huyện theo tỉnh */
    public List<Map<String, Object>> getDistricts(Integer provinceId) {
        String url = ghnConfig.getApiUrl() + "/shiip/public-api/master-data/district";
        Map<String, Object> body = new HashMap<>();
        body.put("province_id", provinceId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
        return extractDataList(response.getBody());
    }

    /** Lấy danh sách phường/xã theo quận */
    public List<Map<String, Object>> getWards(Integer districtId) {
        String url = ghnConfig.getApiUrl() + "/shiip/public-api/master-data/ward";
        Map<String, Object> body = new HashMap<>();
        body.put("district_id", districtId);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });
        return extractDataList(response.getBody());
    }

    // ── Calculate Fee API ─────────────────────────────────────────────────────

    /**
     * Tính phí vận chuyển GHN
     * 
     * @param toDistrictId   quận/huyện người nhận (GHN districtId)
     * @param toWardCode     phường/xã người nhận (GHN wardCode)
     * @param weight         tổng khối lượng (gram)
     * @param length         chiều dài gói hàng (cm)
     * @param width          chiều rộng gói hàng (cm)
     * @param height         chiều cao gói hàng (cm)
     * @param insuranceValue giá trị hàng hóa khai báo (VND)
     * @param serviceTypeId  loại dịch vụ (2=Chuyển phát nhanh, 5=Truyền thống)
     * @param items          danh sách hàng hóa (tùy chọn)
     */
    public Map<String, Object> calculateFee(
            Integer toDistrictId, String toWardCode,
            Integer weight, Integer length, Integer width, Integer height,
            Long insuranceValue, Integer serviceTypeId, List<Map<String, Object>> items) {

        ShippingConfigDTO shopConfig = configService.getShippingConfig();
        Integer fromDistrictId = shopConfig.getGhnDistrictId() != null ? shopConfig.getGhnDistrictId()
                : ghnConfig.getFromDistrictId();
        String fromWardCode = shopConfig.getGhnWardCode() != null && !shopConfig.getGhnWardCode().isEmpty()
                ? shopConfig.getGhnWardCode()
                : ghnConfig.getFromWardCode();

        String url = ghnConfig.getApiUrl() + "/shiip/public-api/v2/shipping-order/fee";
        Map<String, Object> body = new HashMap<>();

        if (fromDistrictId != null && fromDistrictId > 0) {
            body.put("from_district_id", fromDistrictId);
        }
        if (fromWardCode != null && !fromWardCode.isEmpty()) {
            body.put("from_ward_code", fromWardCode);
        }

        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("weight", weight);
        body.put("length", length != null ? length : 20);
        body.put("width", width != null ? width : 20);
        body.put("height", height != null ? height : 10);
        Integer serviceId = getAvailableServiceId(fromDistrictId, toDistrictId);
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id", serviceTypeId != null ? serviceTypeId : ghnConfig.getServiceTypeId());
        }
        if (insuranceValue != null && insuranceValue > 0) {
            body.put("insurance_value", Math.min(insuranceValue, 5_000_000L));
        }
        if (items != null && !items.isEmpty()) {
            body.put("items", items);
        }

        try {
            log.info("GHN calculateFee payload: {}",
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeadersWithShop());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return extractData(response.getBody());
        } catch (Exception e) {
            log.error("GHN calculateFee error: {}", e.getMessage());
            throw new RuntimeException("Không thể tính phí vận chuyển GHN: " + e.getMessage());
        }
    }

    // ── Create Shipping Order API ─────────────────────────────────────────────

    /**
     * Tạo đơn hàng vận chuyển GHN
     * 
     * @return Map chứa order_code, expected_delivery_time, total_fee...
     */
    public Map<String, Object> createShippingOrder(CreateGHNOrderRequest request) {
        String url = ghnConfig.getApiUrl() + "/shiip/public-api/v2/shipping-order/create";

        Map<String, Object> body = new HashMap<>();
        body.put("to_name", request.getToName());
        body.put("to_phone", request.getToPhone());
        body.put("to_address", request.getToAddress());
        body.put("to_ward_code", request.getToWardCode());
        body.put("to_district_id", request.getToDistrictId());
        body.put("weight", request.getWeight());
        body.put("length", request.getLength() != null ? request.getLength() : 20);
        body.put("width", request.getWidth() != null ? request.getWidth() : 20);
        body.put("height", request.getHeight() != null ? request.getHeight() : 10);
        ShippingConfigDTO shopConfig = configService.getShippingConfig();
        Integer fromDistrictId = shopConfig.getGhnDistrictId() != null ? shopConfig.getGhnDistrictId()
                : ghnConfig.getFromDistrictId();

        Integer serviceId = getAvailableServiceId(fromDistrictId, request.getToDistrictId());
        if (serviceId != null) {
            body.put("service_id", serviceId);
        } else {
            body.put("service_type_id",
                    request.getServiceTypeId() != null ? request.getServiceTypeId() : ghnConfig.getServiceTypeId());
        }

        body.put("payment_type_id", request.getPaymentTypeId() != null ? request.getPaymentTypeId() : 1);
        body.put("required_note", request.getRequiredNote() != null ? request.getRequiredNote() : "CHOXEMHANGKHONGTHU");

        if (request.getCodAmount() != null && request.getCodAmount() > 0) {
            body.put("cod_amount", request.getCodAmount());
        }
        if (request.getInsuranceValue() != null && request.getInsuranceValue() > 0) {
            body.put("insurance_value", Math.min(request.getInsuranceValue(), 5_000_000L));
        }
        if (request.getClientOrderCode() != null) {
            body.put("client_order_code", request.getClientOrderCode());
        }
        if (request.getNote() != null) {
            body.put("note", request.getNote());
        }
        if (request.getContent() != null) {
            body.put("content", request.getContent());
        } else {
            body.put("content", "Sản phẩm thời trang"); // Default content if missing
        }

        // Tùy chọn from (nếu có sẽ ghi đè cấu hình Shop)
        if (request.getFromName() != null) {
            body.put("from_name", request.getFromName());
        }
        if (request.getFromPhone() != null) {
            body.put("from_phone", request.getFromPhone());
        }
        
        String fromAddress = request.getFromAddress() != null ? request.getFromAddress() : shopConfig.getDetailAddress();
        if (fromAddress != null) body.put("from_address", fromAddress);
        
        String fromWardName = request.getFromWardName() != null ? request.getFromWardName() : shopConfig.getWardName();
        if (fromWardName != null) body.put("from_ward_name", fromWardName);
        
        String fromDistrictName = request.getFromDistrictName() != null ? request.getFromDistrictName() : shopConfig.getDistrictName();
        if (fromDistrictName != null) body.put("from_district_name", fromDistrictName);
        
        String fromProvinceName = request.getFromProvinceName() != null ? request.getFromProvinceName() : shopConfig.getProvinceName();
        if (fromProvinceName != null) body.put("from_province_name", fromProvinceName);

        // Tùy chọn return (địa chỉ trả hàng)
        if (request.getReturnPhone() != null) body.put("return_phone", request.getReturnPhone());
        if (request.getReturnAddress() != null) body.put("return_address", request.getReturnAddress());
        if (request.getReturnDistrictId() != null) body.put("return_district_id", request.getReturnDistrictId());
        if (request.getReturnWardCode() != null) body.put("return_ward_code", request.getReturnWardCode());

        // Khác
        if (request.getCoupon() != null) body.put("coupon", request.getCoupon());
        if (request.getPickStationId() != null) body.put("pick_station_id", request.getPickStationId());
        if (request.getDeliverStationId() != null) body.put("deliver_station_id", request.getDeliverStationId());
        if (request.getPickShift() != null) body.put("pick_shift", request.getPickShift());

        // Items (required: name, quantity, weight)
        List<Map<String, Object>> items = request.getItems();
        if (items != null && !items.isEmpty()) {
            body.put("items", items);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeadersWithShop());
        try {
            log.info("GHN createShippingOrder payload: {}",
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            log.info("GHN createShippingOrder response: {}",
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(response.getBody()));
            Map<String, Object> data = extractData(response.getBody());
            log.info("GHN Order created: {}", data.get("order_code"));
            return data;
        } catch (Exception e) {
            log.error("GHN createShippingOrder error: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo đơn vận chuyển GHN: " + e.getMessage());
        }
    }

    // ── Order Detail / Tracking API ───────────────────────────────────────────

    /** Lấy thông tin & trạng thái vận chuyển GHN theo mã vận đơn */
    public Map<String, Object> getShippingOrderDetail(String ghnOrderCode) {
        String url = ghnConfig.getApiUrl() + "/shiip/public-api/v2/shipping-order/detail";
        Map<String, Object> body = Map.of("order_code", ghnOrderCode);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            return extractData(response.getBody());
        } catch (Exception e) {
            log.error("GHN getOrderDetail error: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy thông tin vận đơn GHN: " + e.getMessage());
        }
    }

    // ── Print Label API ───────────────────────────────────────────────────────

    /** Tạo print token để in nhãn vận đơn */
    public String generatePrintToken(List<String> orderCodes) {
        String url = ghnConfig.getApiUrl() + "/shiip/public-api/v2/a5/gen-token";
        Map<String, Object> body = Map.of("order_codes", orderCodes);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeadersWithShop());
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> data = extractData(response.getBody());
            return (String) data.get("token");
        } catch (Exception e) {
            log.error("GHN generatePrintToken error: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo token in nhãn GHN: " + e.getMessage());
        }
    }

    /** Lấy URL in nhãn A5 */
    public String getPrintLabelUrl(String printToken) {
        return ghnConfig.getApiUrl() + "/a5/public-api/printA5?token=" + printToken;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractDataList(Map<String, Object> response) {
        if (response == null)
            return List.of();
        Object data = response.get("data");
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractData(Map<String, Object> response) {
        if (response == null)
            return Map.of();
        Object code = response.get("code");
        if (code instanceof Number && ((Number) code).intValue() != 200) {
            String message = (String) response.getOrDefault("message", "GHN API error");
            throw new RuntimeException(message);
        }
        Object data = response.get("data");
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        } else if (data instanceof List) {
            return Map.of("data", data);
        }
        return Map.of();
    }

    private Integer getAvailableServiceId(Integer fromDistrict, Integer toDistrict) {
        if (fromDistrict == null || toDistrict == null || fromDistrict <= 0 || toDistrict <= 0) {
            log.warn("getAvailableServiceId: Bỏ qua vì fromDistrict={} hoặc toDistrict={} không hợp lệ", fromDistrict, toDistrict);
            return null;
        }
        try {
            String url = ghnConfig.getApiUrl() + "/shiip/public-api/v2/shipping-order/available-services";
            Map<String, Object> body = new HashMap<>();
            body.put("shop_id", ghnConfig.getShopId());
            body.put("from_district", fromDistrict);
            body.put("to_district", toDistrict);

            log.info("GHN available-services payload: {}",
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeadersWithShop());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            log.info("GHN available-services response: {}",
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(response.getBody()));

            Object dataObj = response.getBody().get("data");
            if (dataObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
                if (!data.isEmpty()) {
                    for (Map<String, Object> service : data) {
                        if (service.get("service_type_id") != null && ((Number) service.get("service_type_id"))
                                .intValue() == ghnConfig.getServiceTypeId()) {
                            return ((Number) service.get("service_id")).intValue();
                        }
                    }
                    return ((Number) data.get(0).get("service_id")).intValue();
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi lấy service_id GHN: {}", e.getMessage());
        }
        return null; // Trả về null để system tự dùng service_type_id hoặc tự fallback 20K
    }

    // ── Inner Request DTO ─────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateGHNOrderRequest {
        private String toName;
        private String toPhone;
        private String toAddress;
        private String toWardCode;
        private Integer toDistrictId;
        
        // Cấu hình gửi hàng (Tùy chọn, nếu null sẽ lấy mặc định của Shop)
        private String fromName;
        private String fromPhone;
        private String fromAddress;
        private String fromWardName;
        private String fromDistrictName;
        private String fromProvinceName;
        
        // Cấu hình trả hàng (Tùy chọn)
        private String returnPhone;
        private String returnAddress;
        private Integer returnDistrictId;
        private String returnWardCode;
        
        // Các trường khác
        private Integer weight; // gram
        private Integer length; // cm
        private Integer width; // cm
        private Integer height; // cm
        private Integer serviceTypeId; // 2=Express, 5=Standard
        private Integer paymentTypeId; // 1=Shop trả, 2=Người nhận trả
        private String requiredNote; // CHOTHUHANG | CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG
        private Long codAmount;
        private Long insuranceValue;
        private String clientOrderCode;
        private String note;
        private String content;
        private String coupon;
        private Integer pickStationId;
        private Integer deliverStationId;
        private List<Integer> pickShift;
        
        private List<Map<String, Object>> items;
    }
}
