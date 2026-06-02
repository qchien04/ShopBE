package com.scheduled;

import com.constant.PaymentStatus;
import com.entity.Order;
import com.service.implement.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderService orderService;
    private final PayOS payOS;

    @Scheduled(cron = "0 0 2 * * ?")
    public void checkUnpaidOrders() {

        List<Order> unpaidOrders = orderService.findByPaymentStatus(PaymentStatus.UNPAID);

        for (Order order : unpaidOrders) {
            try {
                PaymentLink paymentLink = payOS.paymentRequests()
                        .get(Long.parseLong(order.getOrderNumber().substring(3)));

                if ("PAID".equals(paymentLink.getStatus())) {
                    orderService.updatePaymentStatus(order.getId(), PaymentStatus.PAID, true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Checked unpaid orders at: " + LocalDateTime.now());
    }
}