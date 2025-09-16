package com.damian.photogram.web.user;

import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.service.user.CustomerService;
import com.damian.photogram.web.user.dto.mapper.CustomerDtoMapper;
import com.damian.photogram.web.user.dto.request.CustomerEmailUpdateRequest;
import com.damian.photogram.web.user.dto.response.CustomerDto;
import com.damian.photogram.web.user.dto.response.CustomerWithProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // endpoint to receive current customer data
    @GetMapping("/customers")
    public ResponseEntity<CustomerWithProfileDto> getCustomerData() {
        Customer customer = customerService.getCustomer();
        CustomerWithProfileDto dto = CustomerDtoMapper.toCustomerWithProfileDto(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dto);
    }

    // endpoint to modify current customer email
    @PatchMapping("/customers/email")
    public ResponseEntity<CustomerDto> updateEmail(
            @Validated @RequestBody
            CustomerEmailUpdateRequest request
    ) {
        log.debug("Received request to update customer email");
        Customer customer = customerService.updateEmail(request);
        CustomerDto customerDTO = CustomerDtoMapper.toCustomerDto(customer);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customerDTO);
    }
}

