package com.theatermgnt.theatermgnt.customer.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerAccountCreationRequest;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerProfileUpdateRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.customer.mapper.CustomerMapper;
import com.theatermgnt.theatermgnt.customer.repository.CustomerRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomerService {
    CustomerRepository customerRepository;
    CustomerMapper customerMapper;

    /// CREATE CUSTOMER PROFILE
    @Transactional
    public Customer createCustomerProfile(CustomerAccountCreationRequest request, Account account) {
        Customer customer = customerMapper.toCustomer(request);
        customer.setAccount(account);
        return customerRepository.save(customer);
    }

    /// GET CUSTOMER PROFILE BY ID
    public CustomerResponse getCustomerProfileById(String customerId) {
        Customer customer =
                customerRepository.findById(customerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return customerMapper.toCustomerResponse(customer);
    }

    /// GET MY CUSTOMER PROFILE
    public CustomerResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String accountId = context.getAuthentication().getName();

        Customer customer = customerRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        var customerResponse = customerMapper.toCustomerResponse(customer);
        customerResponse.setNoPassword(
                !StringUtils.hasText(customer.getAccount().getPassword()));
        return customerResponse;
    }

    /// GET ALl CUSTOMERS
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toCustomerResponse)
                .toList();
    }

    /// UPDATE CUSTOMER PROFILE
    @Transactional
    public CustomerResponse updateCustomerProfile(String customerId, CustomerProfileUpdateRequest request) {

        Customer customerToUpdate =
                customerRepository.findById(customerId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        customerMapper.updateCustomerProfile(customerToUpdate, request);
        return customerMapper.toCustomerResponse(customerRepository.save(customerToUpdate));
    }
}
