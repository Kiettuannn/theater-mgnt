package com.theatermgnt.theatermgnt.payment.service;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.theatermgnt.theatermgnt.payment.dto.response.PaymentDetailsResponse;

public interface PaymentService {

    /**
     * Create VNPay payment URL for invoice
     */
    PaymentDetailsResponse createVNPayPayment(
            String invoiceId, HttpServletRequest httpRequest, String returnUrlOverride);

    /**
     * Handle VNPay return URL callback
     */
    Map<String, Object> handleVNPayCallback(Map<String, String> params);

    /**
     * Handle VNPay IPN (Instant Payment Notification)
     */
    Map<String, Object> handleVNPayIPN(Map<String, String> params);

    /**
     * Process cash payment for invoice
     */
    PaymentDetailsResponse processCashPayment(String invoiceId);
}
