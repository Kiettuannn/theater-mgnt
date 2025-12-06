package com.theatermgnt.theatermgnt.notification.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.notification.dto.request.EmailRequest;
import com.theatermgnt.theatermgnt.notification.dto.request.SendEmailRequest;
import com.theatermgnt.theatermgnt.notification.dto.request.Sender;
import com.theatermgnt.theatermgnt.notification.dto.response.EmailResponse;
import com.theatermgnt.theatermgnt.notification.repository.httpClient.EmailClient;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailService {
    EmailClient emailClient;

    @NonFinal
    @Value("${brevo.apiKey}")
    protected String apiKey;

    public EmailResponse sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name("Cifastar")
                        .email("theonlytruth25012005@gmail.com")
                        .build())
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {
            return emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }
}
