package com.service.implement;
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

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentRefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final PayOS payOS;

    @Transactional
    public PaymentTransaction createTransaction(Long orderId, PaymentTransaction.PaymentMethod method, Double amount) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionCode("TXN" + System.currentTimeMillis());
        transaction.setOrder(orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found")));
        transaction.setPaymentMethod(method);
        transaction.setAmount(amount);
        transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);
        return transactionRepository.save(transaction);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return "https://anbato.site";
    }

    private boolean supportPayMentMethod(PaymentTransaction.PaymentMethod method) {
        return true;
    }



    @Transactional
    public CreatePaymentLinkResponse createPaymentLink(HttpServletRequest request,@RequestBody CreatePaymentRequest createPaymentRequest) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        PaymentTransaction.PaymentMethod paymentMethod= createPaymentRequest.getPaymentMethod();

        if(!supportPayMentMethod(paymentMethod)){
            throw new InvalidRequestException("Không hỗ trợ thanh toán!");
        }
        Order order=orderRepository.findByIdWithFullItem(createPaymentRequest.getOrderId(),myId).orElseThrow(
                ()->new NotFoundObjectRequestException("Đơn hàng không tồn tại!")
        );


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

            String returnUrl = baseUrl + "/success?orderId="+orderCode+"&amount="+price+"&method="+String.valueOf(PaymentTransaction.PaymentMethod.BANK_TRANSFER);
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

            return data;

        } catch (Exception e) {
            throw new InvalidRequestException("Some thing went wrong!");

        }
    }



    @Transactional
    public PaymentTransaction updateTransactionStatus(String transactionCode,
                                                      PaymentTransaction.TransactionStatus status) {
        PaymentTransaction transaction = transactionRepository
                .findByTransactionCode(transactionCode)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(status);
        if (status == PaymentTransaction.TransactionStatus.SUCCESS) {
            transaction.setCompletedAt(LocalDateTime.now());
        }
        return transactionRepository.save(transaction);
    }

    @Transactional
    public PaymentRefund createRefund(Long transactionId, Double amount, String reason) {
        PaymentRefund refund = new PaymentRefund();
        refund.setRefundCode("REF" + System.currentTimeMillis());
        refund.setTransaction(transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found")));
        refund.setRefundAmount(amount);
        refund.setReason(reason);
        refund.setStatus(PaymentRefund.RefundStatus.PENDING);
        return refundRepository.save(refund);
    }
}
