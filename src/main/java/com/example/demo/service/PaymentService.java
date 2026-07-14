package com.example.demo.service;

import com.example.demo.DTO.response.PaymentTransactionResponse;
import com.example.demo.Model.Order;
import com.example.demo.exception.PaymentVerificationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repo.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public PaymentService(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    /**
     * Step 1 of checkout: register the internal Order with Razorpay and store
     * the resulting external order ID for later signature verification.
     */
    @Transactional
    public PaymentTransactionResponse createTransaction(Long orderId) {
        Order dbOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            BigDecimal amountInPaise = dbOrder.getTotalAmount().multiply(new BigDecimal("100"));

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise.intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + dbOrder.getId());

            com.razorpay.Order razorpayOrder = client.orders.create(orderRequest);
            String rzpOrderId = razorpayOrder.get("id");

            dbOrder.setRazorpayOrderId(rzpOrderId);
            orderRepository.save(dbOrder);

            return new PaymentTransactionResponse(rzpOrderId, amountInPaise.intValue(), "INR");
        } catch (Exception e) {
            throw new PaymentVerificationException("Error generating payment token: " + e.getMessage());
        }
    }

    /**
     * Step 2: verify the signature returned by Razorpay's client SDK and, if
     * valid, mark the order as PAID via the existing OrderService logic.
     */
    @Transactional
    public Order verifyPayment(Map<String, String> payload) {
        String rzpOrderId = payload.get("razorpay_order_id");
        String rzpPaymentId = payload.get("razorpay_payment_id");
        String rzpSignature = payload.get("razorpay_signature");

        if (rzpOrderId == null || rzpPaymentId == null || rzpSignature == null) {
            throw new PaymentVerificationException("Missing Razorpay verification fields.");
        }

        boolean signatureValid;
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", rzpOrderId);
            options.put("razorpay_payment_id", rzpPaymentId);
            options.put("razorpay_signature", rzpSignature);
            signatureValid = Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            throw new PaymentVerificationException("Verification service failure: " + e.getMessage());
        }

        if (!signatureValid) {
            throw new PaymentVerificationException("Cryptographic signature mismatch. Payment rejected.");
        }

        Order dbOrder = orderRepository.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Orphaned Razorpay Transaction", rzpOrderId));

        dbOrder.setRazorpayPaymentId(rzpPaymentId);
        orderRepository.save(dbOrder);

        return orderService.confirmOrderPayment(dbOrder.getId());
    }
}
