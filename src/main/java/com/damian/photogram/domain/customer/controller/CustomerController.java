package com.damian.photogram.domain.customer.controller;

import com.damian.photogram.domain.customer.dto.request.CustomerEmailUpdateRequest;
import com.damian.photogram.domain.customer.dto.response.CustomerDto;
import com.damian.photogram.domain.customer.dto.response.CustomerWithProfileDto;
import com.damian.photogram.domain.customer.mapper.CustomerDtoMapper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // endpoint to receive logged customer
    @GetMapping("/customers/me")
    public ResponseEntity<CustomerWithProfileDto> getCustomerData() {
        Customer customer = customerService.getCustomer();
        CustomerWithProfileDto dto = CustomerDtoMapper.toCustomerWithProfileDto(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dto);
    }

    // endpoint to modify logged customer email
    @PatchMapping("/customers/me/email")
    public ResponseEntity<CustomerDto> updateEmail(
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        Customer customer = customerService.updateEmail(request);
        CustomerDto customerDTO = CustomerDtoMapper.toCustomerDto(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }
}

