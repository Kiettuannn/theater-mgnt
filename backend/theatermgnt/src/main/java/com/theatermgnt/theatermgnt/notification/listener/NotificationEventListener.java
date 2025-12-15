package com.theatermgnt.theatermgnt.notification.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.theatermgnt.theatermgnt.authentication.event.PasswordResetEvent;
import com.theatermgnt.theatermgnt.notification.dto.request.EmailBuilderRequest;
import com.theatermgnt.theatermgnt.notification.enums.EmailType;
import com.theatermgnt.theatermgnt.notification.service.EmailBuilderService;
import com.theatermgnt.theatermgnt.notification.service.EmailService;
import com.theatermgnt.theatermgnt.notification.service.EmailTemplateFactory;
import com.theatermgnt.theatermgnt.staff.event.StaffCreatedEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationEventListener {
    EmailService emailService;
    EmailTemplateFactory emailTemplateFactory;
    EmailBuilderService emailBuilderService;

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

        emailBuilderService.buildAndSendEmail(EmailBuilderRequest.builder()
                .account(event.getAccount())
                .subject(subject)
                .htmlContent(htmlContent)
                .emailTypeForLog("Password Reset OTP")
                .build());
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
                .subject("Welcome" + event.getStaff().getFirstName() + " to Our Team!")
                .htmlContent(htmlContent)
                .emailTypeForLog("Welcome New Staff")
                .build());
    }
}
