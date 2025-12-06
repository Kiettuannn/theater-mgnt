package com.theatermgnt.theatermgnt.notification.service;

import java.time.Year;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.theatermgnt.theatermgnt.notification.enums.EmailType;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailTemplateFactory {
    TemplateEngine templateEngine;

    public String buildTemplate(EmailType emailType, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        context.setVariable("appName", "Cifastar HCM");
        context.setVariable("companyName", "Cifastar");
        context.setVariable("companyUrl", "https://cifastar.com");
        context.setVariable("companyAddress", "123 Cifastar St, HCM City, Vietnam");
        context.setVariable("year", Year.now().getValue());

        String templateName = switch (emailType) {
            case RESET_PASSWORD -> "email/reset-password";
            case WELCOME_STAFF -> "email/welcome-staff";
            case NOTIFICATION_EMAIL -> "email/notification-email";
        };
        return templateEngine.process(templateName, context);
    }
}
