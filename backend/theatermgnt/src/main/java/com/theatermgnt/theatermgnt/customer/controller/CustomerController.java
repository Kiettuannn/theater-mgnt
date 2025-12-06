package com.theatermgnt.theatermgnt.customer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerProfileUpdateRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;
import com.theatermgnt.theatermgnt.customer.service.CustomerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/customers")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomerController {
    CustomerService customerService;

    @GetMapping("/{customerId}")
    ApiResponse<CustomerResponse> getCustomerProfile(@PathVariable String customerId) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getCustomerProfileById(customerId))
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<CustomerResponse> getMyInfo() {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.getMyInfo())
                .build();
    }

    @GetMapping
    ApiResponse<List<CustomerResponse>> getAllCustomers() {
        return ApiResponse.<List<CustomerResponse>>builder()
                .result(customerService.getAll())
                .build();
    }

    @PutMapping("/{customerId}")
    ApiResponse<CustomerResponse> updateCustomerProfile(
            @PathVariable String customerId, @RequestBody CustomerProfileUpdateRequest request) {
        return ApiResponse.<CustomerResponse>builder()
                .result(customerService.updateCustomerProfile(customerId, request))
                .build();
    }
}
