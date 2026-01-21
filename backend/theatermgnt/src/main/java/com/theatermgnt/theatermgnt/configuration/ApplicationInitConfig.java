package com.theatermgnt.theatermgnt.configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.notification.entity.NotificationChannel;
import com.theatermgnt.theatermgnt.notification.repository.NotificationChannelRepository;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    private PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "driver-class-name",
            havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(
            AccountRepository accountRepository,
            RegistrationService registrationService,
            NotificationChannelRepository channelRepository) {
        return args -> {

            // Initialize notification channels
            initializeNotificationChannels(channelRepository);

            // Check existed username "admin"
            if (accountRepository.findByUsername("admin").isEmpty()) {
                //                var roles = new HashSet<String>();
                //                roles.add(Role.ADMIN.name());

                StaffAccountCreationRequest adminRequest = StaffAccountCreationRequest.builder()
                        .username("admin")
                        .password("admin")
                        .build();

                registrationService.createAdminAccount(adminRequest);
                log.warn("Admin account has been created with default password: admin, please change it");
            }
        };
    }

    private void initializeNotificationChannels(NotificationChannelRepository channelRepository) {
        log.info("Initializing notification channels...");

        // EMAIL Channel
        if (channelRepository.findByName("EMAIL").isEmpty()) {
            NotificationChannel emailChannel =
                    NotificationChannel.builder().name("EMAIL").isActive(true).build();
            channelRepository.save(emailChannel);
            log.info("Created EMAIL channel");
        }

        // IN_APP Channel
        if (channelRepository.findByName("IN_APP").isEmpty()) {
            NotificationChannel inAppChannel =
                    NotificationChannel.builder().name("IN_APP").isActive(true).build();
            channelRepository.save(inAppChannel);
            log.info("Created IN_APP channel");
        }

        // SMS Channel (for future use)
        if (channelRepository.findByName("SMS").isEmpty()) {
            NotificationChannel smsChannel = NotificationChannel.builder()
                    .name("SMS")
                    .isActive(false) // Disabled by default until implemented
                    .build();
            channelRepository.save(smsChannel);
            log.info("Created SMS channel (inactive)");
        }

        log.info("Notification channels initialized successfully");
    }
}
