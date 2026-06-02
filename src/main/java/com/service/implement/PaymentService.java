package com.service.implement;
import com.constant.PaymentMethod;
import com.entity.*;
import com.exception.InvalidRequestException;
import com.exception.NotFoundObjectRequestException;
import com.repository.*;
import com.request.CreatePaymentRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PayOS payOS;
    private final ObjectMapper objectMapper;

    private String getBaseUrl(HttpServletRequest request) {
        return "https://anbato.site";
    }

    private boolean supportPayMentMethod(PaymentMethod method) {
        return true;
    }



    @Transactional
    public Object createPaymentLink(HttpServletRequest request,@RequestBody CreatePaymentRequest createPaymentRequest) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        PaymentMethod paymentMethod= createPaymentRequest.getPaymentMethod();

        if(!supportPayMentMethod(paymentMethod)){
            throw new InvalidRequestException("Không hỗ trợ thanh toán!");
        }
        Order order=orderRepository.findByIdWithFullItem(createPaymentRequest.getOrderId(),myId).orElseThrow(
                ()->new NotFoundObjectRequestException("Đơn hàng không tồn tại!")
        );

        // Nếu đã có dữ liệu link thanh toán, parse JSON và trả về ngay
        if (order.getCheckoutResponseData() != null && !order.getCheckoutResponseData().trim().isEmpty()) {
            try {
                return objectMapper.readValue(order.getCheckoutResponseData(), JsonNode.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<PaymentLinkItem> items=new ArrayList<>();
        for(OrderItem orderItem:order.getItems()){
            PaymentLinkItem item=PaymentLinkItem.builder()
                    .name(orderItem.getProductName())
                    .price(orderItem.getPrice().longValue())
                    .quantity(orderItem.getQuantity())
                    .build();
            items.add(item);
        }
        try {
            String baseUrl = getBaseUrl(request);

            String description = "Thanh toan don hang";

            long price = order.getTotal().longValue();
            long orderCode = Long.parseLong(order.getOrderNumber().substring(3));

            String returnUrl = baseUrl + "/success?orderId="+orderCode+"&amount="+price+"&method="+String.valueOf(PaymentMethod.BANK_TRANSFER);
            String cancelUrl = baseUrl + "/cancel";
            System.out.println(returnUrl);

            CreatePaymentLinkRequest paymentData =
                    CreatePaymentLinkRequest.builder()
                            .orderCode(orderCode)
                            .amount(price)
                            .description(description)
                            .returnUrl(returnUrl)
                            .cancelUrl(cancelUrl)
                            .items(items)
                            .build();

            CreatePaymentLinkResponse data =
                    payOS.paymentRequests().create(paymentData);

            // Lưu toàn bộ dữ liệu phản hồi vào đơn hàng để tái sử dụng
            try {
                order.setCheckoutResponseData(objectMapper.writeValueAsString(data));
                orderRepository.save(order);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return data;

        } catch (Exception e) {
            throw new InvalidRequestException("Some thing went wrong!");

        }
    }
}
