package com.request;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class GHNCreateShippingRequest {
    /** Loại dịch vụ: 2=Chuyển phát nhanh (E-commerce), 5=Truyền thống */
    private Integer serviceTypeId = 2;

    /**
     * Ai trả phí vận chuyển:
     * 1 = Shop/Người gửi trả
     * 2 = Người nhận trả (COD phí ship)
     */
    private Integer paymentTypeId = 1;

    /**
     * Cho phép xem hàng:
     * CHOTHUHANG           - Cho thử hàng
     * CHOXEMHANGKHONGTHU   - Cho xem nhưng không thử
     * KHONGCHOXEMHANG      - Không cho xem hàng
     */
    private String requiredNote = "CHOXEMHANGKHONGTHU";

    /** Tiền thu hộ COD (VND) - 0 nếu đã thanh toán online */
    private Long codAmount = 0L;

    /** Giá trị hàng hóa khai báo bảo hiểm (VND) */
    private Long insuranceValue;

    /** Ghi chú nội bộ cho shipper */
    private String note;

    /** Khối lượng tổng (gram) - mặc định 500g */
    private Integer weight = 500;

    /** Chiều dài gói (cm) */
    private Integer length = 20;

    /** Chiều rộng gói (cm) */
    private Integer width = 20;

    /** Chiều cao gói (cm) */
    private Integer height = 10;

    /** Danh sách items gửi lên GHN (name, quantity, weight) */
    private List<Map<String, Object>> items;
}
