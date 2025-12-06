package com.theatermgnt.theatermgnt.authentication.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.account.service.RegistrationService;
import com.theatermgnt.theatermgnt.authentication.dto.request.ExchangeTokenRequest;
import com.theatermgnt.theatermgnt.authentication.dto.request.OAuthCustomerCreationRequest;
import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.repository.httpClient.OutboundIdentityClient;
import com.theatermgnt.theatermgnt.authentication.repository.httpClient.OutboundUserClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OAuthLoginService {
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    RegistrationService registrationService;
    TokenService tokenService;

    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected String GRANT_TYPE = "authorization_code";

    public AuthenticationResponse loginWithGoogleCode(String code) {
        // 1. Exchange code for access token
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        // 2. Get user info
        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("User info from Google: {}", userInfo);

        // 3. Find or create account and profile
        var account = registrationService.registerOAuthCustomer(OAuthCustomerCreationRequest.builder()
                .email(userInfo.getEmail())
                .firstName(userInfo.getGivenName())
                .lastName(userInfo.getFamilyName())
                .build());

        var token = tokenService.generateToken(account);

        return AuthenticationResponse.builder().token(token).build();
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
