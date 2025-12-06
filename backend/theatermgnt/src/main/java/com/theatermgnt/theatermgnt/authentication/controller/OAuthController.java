package com.theatermgnt.theatermgnt.authentication.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.theatermgnt.theatermgnt.authentication.dto.response.AuthenticationResponse;
import com.theatermgnt.theatermgnt.authentication.service.OAuthLoginService;
import com.theatermgnt.theatermgnt.authentication.service.TokenService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OAuthController {
    OAuthLoginService oAuthLoginService;
    TokenService tokenService;

    @PostMapping("/outbound/authenticate")
    ApiResponse<AuthenticationResponse> outboundAuthentication(@RequestParam("code") String code) {
        var result = oAuthLoginService.loginWithGoogleCode(code);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }
}
