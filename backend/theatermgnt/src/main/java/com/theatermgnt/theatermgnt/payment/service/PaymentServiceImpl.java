package com.theatermgnt.theatermgnt.payment.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theatermgnt.theatermgnt.booking.service.BookingService;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.config.VNPayConfig;
import com.theatermgnt.theatermgnt.payment.dto.response.PaymentDetailsResponse;
import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.entity.InvoiceStatus;
import com.theatermgnt.theatermgnt.payment.entity.Payment;
import com.theatermgnt.theatermgnt.payment.entity.PaymentType;
import com.theatermgnt.theatermgnt.payment.enums.PaymentStatus;
import com.theatermgnt.theatermgnt.payment.mapper.PaymentMapper;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.payment.repository.PaymentMethodRepository;
import com.theatermgnt.theatermgnt.payment.repository.PaymentRepository;
import com.theatermgnt.theatermgnt.payment.util.VNPayUtil;
import com.theatermgnt.theatermgnt.revenue.service.RevenueAggregationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final InvoiceService invoiceService;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;
    private final BookingService bookingService;
    private final RevenueAggregationService revenueAggregationService;

    @Override
    @Transactional
    public PaymentDetailsResponse createVNPayPayment(
            String invoiceId, HttpServletRequest httpRequest, String returnUrlOverride) {
        try {
            // Get invoice
            Invoice invoice = invoiceRepository
                    .findById(invoiceId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));

            // Check if invoice is already paid
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                throw new AppException(ErrorCode.INVOICE_ALREADY_PAID); // custom error
            }

            // Generate transaction reference
            // Compact numeric txnRef (no spaces/dashes), suitable for VNPay
            String txnRef = VNPayUtil.getRandomNumber(12);
            // Compact order info (no spaces/dashes), keep it short
            String sanitized = invoiceId.replace("-", "");
            String orderInfo = "INV" + sanitized.substring(Math.max(0, sanitized.length() - 8));

            // Get VNPay payment method
            var vnpayMethod = paymentMethodRepository.findByName("VNPay").orElseThrow(() -> {
                log.error("VNPay payment method not found in database");
                return new AppException(ErrorCode.PAYMENT_METHOD_NOT_EXISTED);
            });

            // Create payment record
            Payment payment = Payment.builder()
                    .invoiceId(invoiceId)
                    .paymentMethodId(vnpayMethod.getId())
                    .amount(invoice.getTotalAmount())
                    .paymentType(PaymentType.BOOKING)
                    .transactionCode(txnRef)
                    .status(PaymentStatus.PENDING)
                    .description("Payment for invoice: " + invoiceId)
                    .build();
            paymentRepository.save(payment);

            // Build VNPay parameters (TreeMap for sorted keys)
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", vnPayConfig.getVersion());
            vnpParams.put("vnp_Command", vnPayConfig.getCommand());
            vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
            vnpParams.put(
                    "vnp_Amount",
                    String.valueOf(invoice.getTotalAmount().longValue() * 100)); // VNPay requires amount * 100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
            vnpParams.put("vnp_Locale", "vn");
            // Allow runtime override of return URL (e.g., different frontend ports)
            String effectiveReturnUrl = (returnUrlOverride != null && !returnUrlOverride.isBlank())
                    ? returnUrlOverride
                    : vnPayConfig.getReturnUrl();
            vnpParams.put("vnp_ReturnUrl", effectiveReturnUrl);
            vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(httpRequest));

            // Add timestamp (VNPay requires GMT+7)
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Build hash data
            String hashData = VNPayUtil.hashAllFields(vnpParams);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

            // Build payment URL
            String queryUrl = VNPayUtil.getPaymentURL(vnpParams, false);
            String paymentUrl = vnPayConfig.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnpSecureHash;

            log.info("===== VNPAY DEBUG =====");
            log.info("TmnCode: {}", vnPayConfig.getTmnCode());
            log.info("InvoiceId: {}", invoiceId);
            log.info("Amount: {}", invoice.getTotalAmount());
            log.info("TxnRef: {}", txnRef);
            log.info("HashSecret: {}", vnPayConfig.getHashSecret().substring(0, 10) + "...");
            log.info("VNPay Params: {}", vnpParams);
            log.info("Hash Data: {}", hashData);
            log.info("Secure Hash: {}", vnpSecureHash);
            log.info("Query URL: {}", queryUrl);
            log.info("Full Payment URL: {}", paymentUrl);
            log.info("=======================");

            log.info("Created VNPay payment with txnRef: {}, invoiceId: {}", txnRef, invoiceId);

            return PaymentDetailsResponse.builder()
                    .code("00")
                    .message("Success")
                    .paymentUrl(paymentUrl)
                    .id(payment.getId())
                    .transactionCode(txnRef)
                    .invoiceId(invoiceId)
                    .amount(invoice.getTotalAmount())
                    .status(PaymentStatus.PENDING.name())
                    .build();

        } catch (AppException e) {
            log.error("AppException creating VNPay payment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }
    }

    @Override
    public Map<String, Object> handleVNPayCallback(Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verify signature
            String vnpSecureHash = params.get("vnp_SecureHash");

            // Remove hash field before verification
            params.remove("vnp_SecureHash");

            // Use callback hash (raw values, no encoding)
            String hashData = VNPayUtil.hashAllFieldsForCallback(params);
            String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

            log.info("VNPay Callback Verification");

            // Compare hashes (case-insensitive)
            if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
                response.put("code", "97");
                response.put("message", "Invalid signature");
                log.warn("VNPay callback signature mismatch");
                return response;
            }

            // Get payment by transaction code
            String txnRef = params.get("vnp_TxnRef");
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(txnRef);

            if (paymentOpt.isEmpty()) {
                response.put("code", "01");
                response.put("message", "Payment not found");
                return response;
            }

            Payment payment = paymentOpt.get();
            String responseCode = params.get("vnp_ResponseCode");

            // Update payment
            payment.setPaymentDate(LocalDateTime.now());
            if ("00".equals(responseCode)) {
                payment.setStatus(PaymentStatus.SUCCESS);

                // Update invoice status to PAID
                Optional<Invoice> invoiceOpt = invoiceRepository.findById(payment.getInvoiceId());
                if (invoiceOpt.isPresent()) {
                    Invoice invoice = invoiceOpt.get();
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoice.setPaidAt(LocalDateTime.now());
                    invoiceRepository.save(invoice);
                    log.info("Invoice {} marked as PAID", invoice.getId());

                    // Update booking status to CONFIRMED (in separate transaction)
                    try {
                        log.info(
                                "Attempting to confirm booking {} for invoice {}",
                                invoice.getBookingId(),
                                invoice.getId());
                        bookingService.confirmBookingPayment(invoice.getBookingId());
                        log.info("Booking {} confirmed successfully after payment", invoice.getBookingId());
                    } catch (Exception e) {
                        log.error(
                                "Error confirming booking {} for invoice {}: {}",
                                invoice.getBookingId(),
                                invoice.getId(),
                                e.getMessage(),
                                e);
                        // Don't fail callback if booking confirmation fails
                    }
                }

                paymentRepository.save(payment);

                // Aggregate revenue after successful payment (in separate transaction)
                try {
                    revenueAggregationService.processPaymentForRevenue(payment);
                } catch (Exception e) {
                    log.error("Error aggregating revenue for payment {}", payment.getId(), e);
                    // Don't fail the callback, just log the error
                }

                response.put("code", "00");
                response.put("message", "Payment successful");
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                response.put("code", responseCode);
                response.put("message", "Payment failed with code: " + responseCode);
            }

            response.put("paymentId", payment.getId());
            response.put("invoiceId", payment.getInvoiceId());
            // Include bookingId for frontend navigation
            invoiceRepository
                    .findById(payment.getInvoiceId())
                    .ifPresent(inv -> response.put("bookingId", inv.getBookingId()));
            response.put("txnRef", txnRef);

            String amountParam = params.get("vnp_Amount");
            if (amountParam != null) {
                response.put("amount", Long.parseLong(amountParam) / 100);
            }
            // Echo order info if present for UI display
            String orderInfo = params.get("vnp_OrderInfo");
            if (orderInfo != null) {
                response.put("orderInfo", orderInfo);
            }

        } catch (Exception e) {
            log.error("Error handling VNPay callback", e);
            response.put("code", "99");
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Map<String, Object> handleVNPayIPN(Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verify signature
            String vnpSecureHash = params.get("vnp_SecureHash");

            // Remove hash field before verification
            params.remove("vnp_SecureHash");

            // Use callback hash (raw values, no encoding)
            String hashData = VNPayUtil.hashAllFieldsForCallback(params);
            String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);

            log.info("VNPay IPN Verification");

            // Compare hashes (case-insensitive)
            if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                log.warn("VNPay IPN signature mismatch");
                return response;
            }

            String txnRef = params.get("vnp_TxnRef");
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionCode(txnRef);

            if (paymentOpt.isEmpty()) {
                response.put("RspCode", "01");
                response.put("Message", "Payment not found");
                return response;
            }

            Payment payment = paymentOpt.get();

            // Check if already processed
            if (payment.getStatus() != PaymentStatus.PENDING) {
                response.put("RspCode", "02");
                response.put("Message", "Payment already processed");
                return response;
            }

            // Verify amount
            long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
            if (payment.getAmount().longValue() != vnpAmount) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid amount");
                return response;
            }

            // Update payment status
            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaymentDate(LocalDateTime.now());

                // Update invoice status to PAID
                Optional<Invoice> invoiceOpt = invoiceRepository.findById(payment.getInvoiceId());
                if (invoiceOpt.isPresent()) {
                    Invoice invoice = invoiceOpt.get();
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoice.setPaidAt(LocalDateTime.now());
                    invoiceRepository.save(invoice);

                    // Update Booking status to CONFIRMED
                    try {
                        bookingService.confirmBookingPayment(invoice.getBookingId());
                        log.info("Booking {} confirmed after successful payment", invoice.getBookingId());
                    } catch (Exception e) {
                        log.error("Error confirming booking {}", invoice.getBookingId(), e);
                    }

                    log.info("Invoice {} marked as PAID and payment success", invoice.getId());
                }

                paymentRepository.save(payment);

                // Aggregate revenue after successful payment
                try {
                    revenueAggregationService.processPaymentForRevenue(payment);
                } catch (Exception e) {
                    log.error("Error aggregating revenue for payment {}", payment.getId(), e);
                    // Don't fail the IPN, just log the error
                }

                response.put("RspCode", "00");
                response.put("Message", "Confirm success");
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                response.put("RspCode", "00");
                response.put("Message", "Confirm success");
            }

        } catch (Exception e) {
            log.error("Error handling VNPay IPN", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }

        return response;
    }

    @Override
    @Transactional
    public PaymentDetailsResponse processCashPayment(String invoiceId) {
        try {
            log.info("Processing cash payment for invoice: {}", invoiceId);

            // Get invoice
            Invoice invoice = invoiceRepository
                    .findById(invoiceId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_EXISTED));

            // Check if invoice is already paid
            if (invoice.getStatus() == InvoiceStatus.PAID) {
                throw new AppException(ErrorCode.INVOICE_ALREADY_PAID); // Invoice already paid
            }

            // Get Cash payment method
            var cashMethod = paymentMethodRepository.findByName("Cash").orElseThrow(() -> {
                log.error("Cash payment method not found in database");
                return new AppException(ErrorCode.PAYMENT_METHOD_NOT_EXISTED);
            });

            // Generate transaction code for cash payment
            String txnRef = "CASH" + VNPayUtil.getRandomNumber(10);

            // Create payment record
            Payment payment = Payment.builder()
                    .invoiceId(invoiceId)
                    .paymentMethodId(cashMethod.getId())
                    .amount(invoice.getTotalAmount())
                    .paymentType(PaymentType.BOOKING)
                    .transactionCode(txnRef)
                    .status(PaymentStatus.SUCCESS)
                    .paymentDate(LocalDateTime.now())
                    .description("Cash payment for invoice: " + invoiceId)
                    .build();
            paymentRepository.save(payment);

            // Update invoice status to PAID
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            log.info("Invoice {} marked as PAID", invoice.getId());

            // Update booking status to CONFIRMED
            try {
                log.info("Confirming booking {} for invoice {}", invoice.getBookingId(), invoice.getId());
                bookingService.confirmBookingPayment(invoice.getBookingId());
                log.info("Booking {} confirmed successfully after cash payment", invoice.getBookingId());
            } catch (Exception e) {
                log.error(
                        "Error confirming booking {} for invoice {}: {}",
                        invoice.getBookingId(),
                        invoice.getId(),
                        e.getMessage(),
                        e);
                throw e; // Rollback transaction if booking confirmation fails
            }

            // Aggregate revenue after successful payment
            try {
                revenueAggregationService.processPaymentForRevenue(payment);
            } catch (Exception e) {
                log.error("Error aggregating revenue for payment {}", payment.getId(), e);
                // Don't fail the payment, just log the error
            }

            log.info("Cash payment processed successfully for invoice: {}", invoiceId);

            return PaymentDetailsResponse.builder()
                    .code("00")
                    .message("Cash payment successful")
                    .id(payment.getId())
                    .transactionCode(txnRef)
                    .invoiceId(invoiceId)
                    .amount(invoice.getTotalAmount())
                    .status(PaymentStatus.SUCCESS.name())
                    .build();

        } catch (AppException e) {
            log.error("AppException processing cash payment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing cash payment", e);
            throw new AppException(ErrorCode.BOOKING_NOT_EXISTED);
        }
    }
}
