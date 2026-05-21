package com.controller;

import com.request.CreatePaymentRequest;
import com.response.ApiResponse2;
import com.service.implement.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.ConfirmWebhookResponse;

@RestController
@RequestMapping(("/check-out"))
public class CheckoutController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/")
    public String index() {
        return "payOS API is running";
    }

    @GetMapping("/success")
    public String success() {
        return "Payment success";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "Payment cancelled";
    }


    @PostMapping(
            value = "/create-payment-link")
    public ResponseEntity<?> checkout(HttpServletRequest request,@RequestBody CreatePaymentRequest createPaymentRequest) {
        Object paymentData = paymentService.createPaymentLink(request,createPaymentRequest);

        return ResponseEntity.ok(paymentData);
    }

}
