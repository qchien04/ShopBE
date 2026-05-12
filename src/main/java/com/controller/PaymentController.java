package com.controller;
import com.entity.PaymentTransaction;
import com.exception.NoPermissionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.request.MyConfirmWebhookRequest;
import com.response.ApiResponse2;
import com.service.implement.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.core.FileDownloadResponse;
import vn.payos.exception.APIException;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.invoices.InvoicesInfo;
import vn.payos.model.webhooks.ConfirmWebhookResponse;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayOS payOS;

    @Value("${domain}")
    private String domain;

    @PostMapping(path = "/confirm-webhook")
    public ApiResponse2<ConfirmWebhookResponse> confirmWebhook(@RequestBody(required = false) MyConfirmWebhookRequest rq) {
        try {
            Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            System.out.println(myId);
            if(myId!=1) throw new NoPermissionException("Cut ra ngoai!");

            String cfDomain = rq != null ? rq.getDomain() : null;

            if (cfDomain == null || cfDomain.isBlank()) {
                cfDomain = domain;
            }

            String finalUrl=cfDomain+"/api/payments/payos_transfer_handler";
            System.out.println(finalUrl);
            ConfirmWebhookResponse result = payOS.webhooks().confirm(finalUrl);
            return ApiResponse2.success("ok", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse2.error(e.getMessage());
        }
    }

    @PostMapping(path = "/payos_transfer_handler")
    public ApiResponse2<WebhookData> payosTransferHandler(@RequestBody Object body)
            throws JsonProcessingException, IllegalArgumentException {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            System.out.println(data);
            if ("00".equals(data.getCode())) {
                orderService.updatePaymentStatus(
                        "ORD" + data.getOrderCode(),
                        PaymentTransaction.PaymentStatus.PAID
                );
            }
            return ApiResponse2.success("Webhook delivered", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse2.error(e.getMessage());
        }
    }


    @GetMapping(path = "/{orderId}")
    public ApiResponse2<PaymentLink> getOrderById(@PathVariable("orderId") String orderId) {
        try {
            PaymentLink order = payOS.paymentRequests().get(Long.parseLong(orderId.substring(3)));
            return ApiResponse2.success("ok", order);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse2.error(e.getMessage());
        }
    }

    @PutMapping(path = "/{orderId}")
    public ApiResponse2<PaymentLink> cancelOrder(@PathVariable("orderId") long orderId) {
        try {
            PaymentLink order = payOS.paymentRequests().cancel(orderId, "change my mind");
            return ApiResponse2.success("ok", order);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse2.error(e.getMessage());
        }
    }



    @GetMapping(path = "/{orderId}/invoices")
    public ApiResponse2<InvoicesInfo> retrieveInvoices(@PathVariable("orderId") long orderId) {
        try {
            InvoicesInfo invoicesInfo = payOS.paymentRequests().invoices().get(orderId);
            return ApiResponse2.success("ok", invoicesInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse2.error(e.getMessage());
        }
    }

    @GetMapping(path = "/{orderId}/invoices/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(
            @PathVariable("orderId") long orderId, @PathVariable("invoiceId") String invoiceId) {
        try {
            FileDownloadResponse invoiceFile =
                    payOS.paymentRequests().invoices().download(invoiceId, orderId);

            if (invoiceFile == null || invoiceFile.getData() == null) {
                return ResponseEntity.status(404).body(ApiResponse2.error("invoice not found or empty"));
            }

            ByteArrayResource resource = new ByteArrayResource(invoiceFile.getData());

            HttpHeaders headers = new HttpHeaders();
            String contentType =
                    invoiceFile.getContentType() == null
                            ? MediaType.APPLICATION_PDF_VALUE
                            : invoiceFile.getContentType();
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + invoiceFile.getFilename() + "\"");
            if (invoiceFile.getSize() != null) {
                headers.setContentLength(invoiceFile.getSize());
            }

            return ResponseEntity.ok().headers(headers).body(resource);
        } catch (APIException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiResponse2.error(e.getErrorDesc().orElse(e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse2.error(e.getMessage()));
        }
    }


//    @PostMapping("/transactions")
//    public ResponseEntity<PaymentTransaction> createTransaction(
//            @RequestBody CreateTransactionRequest request) {
//        PaymentTransaction transaction = paymentService.createTransaction(
//                request.getOrderId(),
//                request.getPaymentMethod(),
//                request.getAmount()
//        );
//        return ResponseEntity.ok(transaction);
//    }
//
//    @PutMapping("/transactions/{transactionCode}/status")
//    public ResponseEntity<PaymentTransaction> updateTransactionStatus(
//            @PathVariable String transactionCode,
//            @RequestParam PaymentTransaction.TransactionStatus status) {
//        return ResponseEntity.ok(paymentService.updateTransactionStatus(transactionCode, status));
//    }
//
//    @PostMapping("/refunds")
//    public ResponseEntity<PaymentRefund> createRefund(@RequestBody CreateRefundRequest request) {
//        PaymentRefund refund = paymentService.createRefund(
//                request.getTransactionId(),
//                request.getAmount(),
//                request.getReason()
//        );
//        return ResponseEntity.ok(refund);
//    }
}
