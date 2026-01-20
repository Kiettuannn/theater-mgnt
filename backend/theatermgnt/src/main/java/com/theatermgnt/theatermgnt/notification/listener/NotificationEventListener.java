package com.theatermgnt.theatermgnt.notification.listener;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.authentication.event.PasswordResetEvent;
import com.theatermgnt.theatermgnt.booking.entity.Booking;
import com.theatermgnt.theatermgnt.booking.repository.BookingRepository;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.customer.event.CustomerCreatedEvent;
import com.theatermgnt.theatermgnt.customer.repository.CustomerRepository;
import com.theatermgnt.theatermgnt.notification.dto.request.EmailBuilderRequest;
import com.theatermgnt.theatermgnt.notification.enums.EmailType;
import com.theatermgnt.theatermgnt.notification.service.EmailBuilderService;
import com.theatermgnt.theatermgnt.notification.service.EmailTemplateFactory;
import com.theatermgnt.theatermgnt.payment.entity.Invoice;
import com.theatermgnt.theatermgnt.payment.event.InvoiceRefundedEvent;
import com.theatermgnt.theatermgnt.payment.repository.InvoiceRepository;
import com.theatermgnt.theatermgnt.staff.event.StaffCreatedEvent;
import com.theatermgnt.theatermgnt.ticket.dto.response.TicketEmailView;
import com.theatermgnt.theatermgnt.ticket.entity.Ticket;
import com.theatermgnt.theatermgnt.ticket.event.TicketCreatedEvent;
import com.theatermgnt.theatermgnt.ticket.repository.TicketRepository;
import com.theatermgnt.theatermgnt.ticket.service.QrImageGenerator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import sibModel.SendSmtpEmailAttachment;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationEventListener {
    EmailTemplateFactory emailTemplateFactory;
    EmailBuilderService emailBuilderService;
    QrImageGenerator qrImageGenerator;
    BookingRepository bookingRepository;
    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    TicketRepository ticketRepository;
    InvoiceRepository invoiceRepository;

    @NonFinal
    @Value("${otp.valid-duration}")
    protected long OTP_VALID_DURATION;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetEvent(PasswordResetEvent event) {
        log.info(
                "Handling password reset OTP event for email: {}",
                event.getAccount().getEmail());

        String subject = "Prove Your Cifastar HCM Identity";

        Map<String, Object> variables = Map.of(
                "subject", subject,
                "username", event.getAccount().getUsername(),
                "otpCode", event.getOtpCode(),
                "email", event.getAccount().getEmail(),
                "otpDuration", OTP_VALID_DURATION);

        String htmlContent = emailTemplateFactory.buildTemplate(EmailType.RESET_PASSWORD, variables);

        try {
            emailBuilderService.buildAndSendEmail(EmailBuilderRequest.builder()
                    .account(event.getAccount())
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .emailTypeForLog("Password Reset OTP")
                    .build());
        } catch (Exception e) {
            log.error("=== FAILED TO SEND PASSWORD RESET EMAIL ===", e);
            throw e;
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStaffCreatedEvent(StaffCreatedEvent event) {
        log.info(
                "Sending welcome email to new staff: {}",
                event.getStaff().getAccount().getEmail());

        Map<String, Object> variables = Map.of(
                "subject", "Welcome to Our Team!",
                "name", event.getStaff().getFirstName(),
                "username", event.getStaff().getAccount().getUsername(),
                "password", event.getRawPassword(),
                "loginUrl", "http://localhost:5173/admin/login");

        String htmlContent = emailTemplateFactory.buildTemplate(EmailType.WELCOME_STAFF, variables);

        emailBuilderService.buildAndSendEmail(EmailBuilderRequest.builder()
                .account(event.getStaff().getAccount())
                .subject("Welcome " + event.getStaff().getFirstName() + " to Our Team!")
                .htmlContent(htmlContent)
                .emailTypeForLog("Welcome New Staff")
                .build());
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTicketCreatedEvent(TicketCreatedEvent event) {
        Booking booking = bookingRepository.findById(event.getBookingId()).orElseThrow();

        Account account =
                accountRepository.findById(String.valueOf(event.getAccountId())).orElseThrow();

        List<Ticket> tickets = ticketRepository.findAllForEmail(event.getTicketIds());
        ;

        log.info("Sending ticket email to {}", account.getEmail());

        String subject = "Your Movie Ticket – Cifastar HCM";

        List<TicketEmailView> ticketViews = tickets.stream()
                .map(t -> TicketEmailView.builder()
                        .seatCode(t.getSeatName())
                        .seatType(t.getScreeningSeat().getSeat().getSeatType().getTypeName())
                        .ticketPrice(t.getPrice())
                        .ticketCode(t.getTicketCode())
                        .build())
                .toList();

        List<SendSmtpEmailAttachment> attachments = tickets.stream()
                .map(t -> {
                    byte[] qrBytes = Base64.getDecoder().decode(qrImageGenerator.generateBase64Qr(t.getQrContent()));

                    SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
                    attachment.setName("QR-" + t.getTicketCode() + ".png");
                    attachment.setContent(qrBytes);
                    return attachment;
                })
                .toList();

        Map<String, Object> variables = Map.of(
                "subject",
                subject,
                "username",
                account.getUsername(),
                "bookingCode",
                booking.getId(),
                "movieName",
                booking.getScreening().getMovie().getTitle(),
                "showTime",
                booking.getScreening().getStartTime(),
                "cinema",
                booking.getScreening().getRoom().getCinema().getName(),
                "tickets",
                ticketViews,
                "totalPrice",
                booking.getTotalAmount(),
                "email",
                account.getEmail());

        String htmlContent = emailTemplateFactory.buildTemplate(EmailType.TICKET_ISSUE, variables);

        emailBuilderService.buildAndSendEmail(EmailBuilderRequest.builder()
                .account(account)
                .subject(subject)
                .htmlContent(htmlContent)
                .attachments(attachments)
                .emailTypeForLog("Ticket Issued")
                .build());
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCustomerCreatedEvent(CustomerCreatedEvent event) {

        Customer customer = customerRepository.findById(event.getCustomerId()).orElseThrow();
        log.info(
                "Sending welcome email to new customer: {}",
                customer.getAccount().getEmail());

        Map<String, Object> variables = Map.of(
                "subject",
                "Welcome to Cifastar HCM!",
                "name",
                customer.getLastName() + " " + customer.getFirstName(),
                "username",
                customer.getAccount().getEmail(),
                "password",
                event.getRawPassword(),
                "loginUrl",
                "http://localhost:3000");

        String htmlContent = emailTemplateFactory.buildTemplate(EmailType.WELCOME_CUSTOMER, variables);

        emailBuilderService.buildAndSendEmail(EmailBuilderRequest.builder()
                .account(customer.getAccount())
                .subject("Welcome " + customer.getFirstName() + " to Cifastar HCM!")
                .htmlContent(htmlContent)
                .emailTypeForLog("Welcome New Customer")
                .build());
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceRefundedEvent(InvoiceRefundedEvent event) {
        try {
            Invoice invoice = invoiceRepository.findById(event.getInvoiceId()).orElseThrow();
            Booking booking = bookingRepository
                    .findById(java.util.UUID.fromString(event.getBookingId()))
                    .orElseThrow();
            Account account =
                    booking.getCustomer() != null ? booking.getCustomer().getAccount() : null;

            if (account == null
                    || account.getEmail() == null
                    || account.getEmail().isBlank()) {
                log.info("No customer email for booking {}, skipping refund email", event.getBookingId());
                return;
            }

            String subject = "Your Refund Has Been Processed – Cifastar HCM";

            java.util.Map<String, Object> variables = java.util.Map.of(
                    "subject", subject,
                    "username", account.getUsername(),
                    "bookingCode", booking.getId(),
                    "movieName", booking.getScreening().getMovie().getTitle(),
                    "showTime", booking.getScreening().getStartTime(),
                    "cinema", booking.getScreening().getRoom().getCinema().getName(),
                    "refundAmount", booking.getTotalAmount(),
                    "email", account.getEmail());

            String htmlContent = emailTemplateFactory.buildTemplate(EmailType.REFUND_NOTIFICATION, variables);

            emailBuilderService.buildAndSendEmail(EmailBuilderRequest.builder()
                    .account(account)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .emailTypeForLog("Invoice Refunded")
                    .build());
        } catch (Exception e) {
            log.error("Failed to send refund email for invoice {}", event.getInvoiceId(), e);
        }
    }
}
