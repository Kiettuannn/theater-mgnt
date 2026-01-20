package com.theatermgnt.theatermgnt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VNPayConfig {
    private String tmnCode;
    private String hashSecret;
    private String url;
    private String returnUrl;
    private String ipnUrl;
    private String version;
    private String command;
    private String orderType;
}
