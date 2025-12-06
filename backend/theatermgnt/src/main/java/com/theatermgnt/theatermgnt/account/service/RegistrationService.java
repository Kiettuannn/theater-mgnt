package com.theatermgnt.theatermgnt.account.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.account.repository.AccountRepository;
import com.theatermgnt.theatermgnt.authentication.dto.request.OAuthCustomerCreationRequest;
import com.theatermgnt.theatermgnt.authentication.enums.AccountType;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.authorization.repository.RoleRepository;
import com.theatermgnt.theatermgnt.constant.PredefinedRole;
import com.theatermgnt.theatermgnt.customer.dto.request.CustomerAccountCreationRequest;
import com.theatermgnt.theatermgnt.customer.dto.response.CustomerResponse;
import com.theatermgnt.theatermgnt.customer.entity.Customer;
import com.theatermgnt.theatermgnt.customer.mapper.CustomerMapper;
import com.theatermgnt.theatermgnt.customer.repository.CustomerRepository;
import com.theatermgnt.theatermgnt.customer.service.CustomerService;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;
import com.theatermgnt.theatermgnt.staff.dto.response.StaffResponse;
import com.theatermgnt.theatermgnt.staff.entity.Staff;
import com.theatermgnt.theatermgnt.staff.event.StaffCreatedEvent;
import com.theatermgnt.theatermgnt.staff.mapper.StaffMapper;
import com.theatermgnt.theatermgnt.staff.service.StaffService;
import org.springframework.context.ApplicationEventPublisher;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RegistrationService {
    AccountService accountService;
    CustomerService customerService;
    StaffService staffService;
    CustomerMapper customerMapper;
    StaffMapper staffMapper;
    RoleRepository roleRepository;
    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public CustomerResponse registerCustomerAccount(CustomerAccountCreationRequest request) {
        Account savedAccount = accountService.createAccount(request);
        savedAccount.setAccountType(AccountType.CUSTOMER);

        Customer savedCustomer = customerService.createCustomerProfile(request, savedAccount);
        return customerMapper.toCustomerResponse(savedCustomer);
    }

    @Transactional
    public Account registerOAuthCustomer(OAuthCustomerCreationRequest request) {
        return accountRepository.findByEmail(request.getEmail()).orElseGet(() -> {
            ;
            Account newAccount = Account.builder()
                    .email(request.getEmail())
                    .username(request.getEmail())
                    .accountType(AccountType.CUSTOMER)
                    .isActive(true)
                    .build();
            Account savedAccount = accountRepository.save(newAccount);

            Customer newCustomer = Customer.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .account(savedAccount)
                    .build();
            customerRepository.save(newCustomer);
            return savedAccount;
        });
    }

    /// Create staff account
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StaffResponse registerStaffAccount(StaffAccountCreationRequest request) {
        Set<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.STAFF_ROLE).ifPresent(roles::add);
        return internalCreateStaff(request, roles);
    }

    /// Only use for initial admin account creation
    @Transactional
    public StaffResponse createAdminAccount(StaffAccountCreationRequest request) {
        Set<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.ADMIN_ROLE).ifPresent(roles::add);
        return internalCreateStaff(request, roles);
    }

    private StaffResponse internalCreateStaff(StaffAccountCreationRequest request, Set<Role> roles) {
        Account savedAccount = accountService.createAccount(request);
        savedAccount.setAccountType(AccountType.INTERNAL);

        Staff savedStaff = staffService.createStaffProfile(request, savedAccount, roles);
        eventPublisher.publishEvent(new StaffCreatedEvent(savedStaff,request.getPassword()));

        return staffMapper.toStaffResponse(savedStaff);
    }
}
